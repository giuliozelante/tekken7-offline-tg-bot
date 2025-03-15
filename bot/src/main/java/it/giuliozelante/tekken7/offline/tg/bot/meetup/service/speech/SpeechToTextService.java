package it.giuliozelante.tekken7.offline.tg.bot.meetup.service.speech;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for converting speech to text using CMU Sphinx.
 * Optimized for Italian language recognition.
 */
@Singleton
@Slf4j
public class SpeechToTextService {

    private final Configuration configuration;

    public SpeechToTextService() {
        // Configure CMU Sphinx
        configuration = new Configuration();
        
        // Set path to Italian acoustic model (fallback to English if Italian not available)
        try {
            // Try to use Italian acoustic model if available
            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/it-it/it-it");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/it-it/it-it.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/it-it/it-it.lm.bin");
            log.info("Italian language model loaded for speech recognition");
        } catch (Exception e) {
            // Fallback to English if Italian model is not available
            log.warn("Italian language model not found, falling back to English", e);
            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
        }
        
        log.info("Speech-to-text service initialized");
    }

    /**
     * Converts voice message audio to text.
     * 
     * @param audioInputStream The input stream of the voice message audio
     * @return The recognized text or null if recognition failed
     */
    public String convertSpeechToText(InputStream audioInputStream) {
        File tempFile = null;
        try {
            // Create a temporary file for the audio
            tempFile = createTempFile(audioInputStream);
            
            // Create a speech recognizer
            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            
            // Start recognition
            InputStream fileStream = Files.newInputStream(tempFile.toPath());
            recognizer.startRecognition(fileStream);
            
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
        } catch (Exception e) {
            log.error("Error converting speech to text", e);
            return null;
        } finally {
            // Clean up the temporary file
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
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
} 