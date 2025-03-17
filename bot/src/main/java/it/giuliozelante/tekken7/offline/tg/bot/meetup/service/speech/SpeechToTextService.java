package it.giuliozelante.tekken7.offline.tg.bot.meetup.service.speech;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacket;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.opus.OpusInfo;
import org.gagravarr.opus.OpusPacketFactory;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import io.github.jaredmdobson.concentus.OpusDecoder;
import io.github.jaredmdobson.concentus.OpusException;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class SpeechToTextService {
    private static final int OPUS_SAMPLE_RATE = 48000;
    private static final int FRAME_SIZE = 960; // 20ms at 48kHz
    private static final int MAX_FRAME_SIZE = 5760;

    private final Configuration configuration;
    private final StreamSpeechRecognizer recognizer;
    private static final float SAMPLE_RATE = 48000.0f;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    public SpeechToTextService() throws IOException {
        // Configure CMU Sphinx for Italian
        configuration = new Configuration();

        // Set paths to Italian acoustic model and dictionary
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/it-it/acoustic-model");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/it-it/voxforge_it_sphinx.dic");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/it-it/voxforge_it_sphinx.lm");

        try {
            recognizer = new StreamSpeechRecognizer(configuration);
            log.info("Speech-to-text service initialized with Italian language model");
        } catch (IOException e) {
            log.error("Failed to initialize speech recognizer with Italian model", e);
            throw e;
        }
    }

    /**
     * Converts voice message audio to text.
     * 
     * @param audioInputStream The input stream of the voice message audio
     * @return The recognized text or null if recognition failed
     */
    public String convertSpeechToText(InputStream audioInputStream) {
        File tempFile = null;
        File convertedFile = null;
        try {
            // Create a temporary file for the audio
            tempFile = createTempFile(audioInputStream);
            log.debug("Created temporary audio file: {}", tempFile);

            // Convert the audio to the required format
            convertedFile = convertAudioToWav(tempFile);
            if (convertedFile == null) {
                log.error("Failed to convert audio to required format");
                return null;
            }
            log.debug("Converted to WAV file: {}", convertedFile);

            // Start recognition with converted audio
            try (AudioInputStream wavStream = AudioSystem.getAudioInputStream(convertedFile)) {
                // Get audio format details for debugging
                AudioFormat format = wavStream.getFormat();
                log.debug("Audio format - Sample rate: {} Hz, Sample size: {} bits, Channels: {}, Encoding: {}",
                        format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(),
                        format.getEncoding());

                // Convert to raw PCM if needed
                AudioFormat targetFormat = new AudioFormat(
                        SAMPLE_RATE,
                        SAMPLE_SIZE_IN_BITS,
                        CHANNELS,
                        SIGNED,
                        BIG_ENDIAN);

                AudioInputStream convertedStream;
                if (!format.matches(targetFormat)) {
                    log.debug("Converting audio stream to target format");
                    convertedStream = AudioSystem.getAudioInputStream(targetFormat, wavStream);
                } else {
                    convertedStream = wavStream;
                }

                // Start recognition
                recognizer.startRecognition(convertedStream);

                // Get the recognition result
                StringBuilder resultText = new StringBuilder();
                SpeechResult result;
                while ((result = recognizer.getResult()) != null) {
                    String hypothesis = result.getHypothesis();
                    if (!hypothesis.isEmpty()) {
                        resultText.append(hypothesis).append(" ");
                    }
                }

                // Stop recognition
                recognizer.stopRecognition();

                String recognizedText = resultText.toString().trim();
                log.debug("Recognized text: {}", recognizedText);

                return recognizedText.isEmpty() ? null : recognizedText;
            }
        } catch (IOException e) {
            log.error("Error handling audio file", e);
            return null;
        } catch (RuntimeException e) {
            log.error("Error during speech recognition", e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during speech processing", e);
            return null;
        } finally {
            // Clean up the temporary files
            deleteIfExists(tempFile);
            deleteIfExists(convertedFile);
        }
    }

    /**
     * Creates a temporary file from the input stream.
     * 
     * @param inputStream The input stream to save as a temporary file
     * @return The temporary file
     * @throws IOException If an I/O error occurs
     */
    private File createTempFile(InputStream inputStream) throws IOException {
        Path tempFilePath = Files.createTempFile("voice_message_", ".ogg");
        Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        return tempFilePath.toFile();
    }

    /**
     * Converts the input audio file to WAV format with required specifications.
     * 
     * @param inputFile The input audio file
     * @return The converted WAV file, or null if conversion fails
     * @throws OpusException if there is an error decoding the Opus audio
     */
    private File convertAudioToWav(File inputFile) throws OpusException {
        try (FileInputStream fis = new FileInputStream(inputFile); OggFile oggFile = new OggFile(fis)) {
            OggPacketReader reader = oggFile.getPacketReader();

            // Read the first packet which should be OpusHead
            OggPacket firstPacket = reader.getNextPacket();
            if (firstPacket == null) {
                throw new IOException("Invalid Opus file: no packets found");
            }

            OpusInfo info = (OpusInfo) OpusPacketFactory.create(firstPacket);
            int channelCount = info.getNumChannels();

            log.debug("Opus file details:");
            log.debug("Channels: " + channelCount);
            log.debug("Pre-skip: " + info.getPreSkip());
            log.debug("Original sampling rate: " + info.getRate());

            // We're decoding to 48kHz PCM
            int sampleRate = OPUS_SAMPLE_RATE;

            // Initialize the Opus decoder
            OpusDecoder decoder = new OpusDecoder(sampleRate, channelCount);

            // Skip the second packet which is OpusTags
            reader.getNextPacket();

            // Define the target format (16kHz, 16-bit, mono, signed, little-endian)
            AudioFormat targetFormat = new AudioFormat(
                    SAMPLE_RATE,
                    SAMPLE_SIZE_IN_BITS,
                    CHANNELS,
                    SIGNED,
                    BIG_ENDIAN);

            // Create WAV file
            File wavFile = File.createTempFile("converted_", ".wav");

            // Create a temporary buffer for decoded PCM data
            ByteArrayOutputStream pcmData = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(pcmData);

            // Create buffers for decoding
            short[] decodedAudio = new short[MAX_FRAME_SIZE * channelCount];
            byte[] packetData;
            OggPacket packet;

            // Decode each Opus packet
            while ((packet = reader.getNextPacket()) != null) {
                packetData = packet.getData();

                // Skip any non-audio packets
                if (packetData.length <= 0) {
                    continue;
                }

                // Decode the Opus packet to PCM
                int samplesDecoded = decoder.decode(packetData, 0, packetData.length, decodedAudio, 0, FRAME_SIZE,
                        false);

                // Write the decoded PCM data
                for (int i = 0; i < samplesDecoded * channelCount; i++) {
                    dataOut.writeShort(Short.reverseBytes(decodedAudio[i])); // Ensure correct endianness
                }
            }

            // Convert the PCM data to an AudioInputStream
            byte[] completeAudioData = pcmData.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(completeAudioData);
            AudioInputStream audioInputStream = new AudioInputStream(
                    bais,
                    targetFormat,
                    completeAudioData.length / targetFormat.getFrameSize());

            // Write the WAV file
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);
            return wavFile;
        } catch (IOException | IllegalArgumentException e) {
            log.error("Error converting audio format: {}", e.getMessage(), e);
            return null;
        }

    }

    /**
     * Safely deletes a file if it exists.
     * 
     * @param file The file to delete
     */
    private void deleteIfExists(File file) {
        if (file != null && file.exists()) {
            try {
                Files.delete(file.toPath());
                log.debug("Deleted temporary file: {}", file);
            } catch (IOException e) {
                log.warn("Failed to delete temporary file: {}", file, e);
            }
        }
    }
}