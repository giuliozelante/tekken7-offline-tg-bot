# Speech-to-Text Feature for Tekken7 Offline Telegram Bot

## Overview

This feature adds voice message recognition capabilities to the Tekken7 Offline Telegram Bot. Users can now send voice messages to the bot, which will be converted to text using the CMU Sphinx speech recognition library. The feature is optimized for Italian language recognition.

## Features

- Voice message recognition using CMU Sphinx
- Optimized for Italian language with English fallback
- Support for command recognition in voice messages
- Automatic handling of recognized text as if it was typed
- Helpful command (`/voice_message_help`) to explain the feature

## Technical Implementation

### Dependencies

The feature uses the following dependencies:

- `edu.cmu.sphinx:sphinx4-core:5prealpha`
- `edu.cmu.sphinx:sphinx4-data:5prealpha`

### Components

1. **SpeechToTextService**: A service that handles the conversion of voice messages to text using CMU Sphinx, configured for Italian language recognition.
2. **Voice Message Handling**: Added to the MeetUp class to process voice messages from Telegram.
3. **Command Initialization**: A service to initialize the voice message help command in the database.

## Usage

1. Send a voice message to the bot
2. The bot will convert your speech to text using CMU Sphinx
3. The bot will respond to your voice message as if you had typed it

You can even send commands via voice messages by starting with a slash (/).

## Limitations

- Speech recognition works best in quiet environments with clear speech
- CMU Sphinx is lightweight but not as accurate as cloud-based solutions
- Italian language model may not be as comprehensive as the English one

## Future Improvements

- Improve Italian language recognition accuracy
- Add support for additional languages
- Add custom vocabulary for Tekken-specific terms
- Consider using a more advanced speech recognition service for better accuracy
