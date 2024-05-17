# GPT-Assistant

![Build](https://github.com/Feddericokz/GPT-Assistant/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/com.github.feddericokz.gptassistant.svg)](https://plugins.jetbrains.com/plugin/com.github.feddericokz.gptassistant)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/com.github.feddericokz.gptassistant.svg)](https://plugins.jetbrains.com/plugin/com.github.feddericokz.gptassistant)


<!-- Plugin description -->

## Description

This plugin creates a coding assistant using your OpenIA API key. The assistant integrates with the IDE allowing it 
to replace selections of code following instructions in the comments. It can also create files if the assistant thinks 
it needs to, or if asked to do so.
It gives manual control over what is sent as context for the assistant to understand your request.

<!-- Plugin description end -->

## Installation

### Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "GPT-Assistant"</kbd> >
  <kbd>Install</kbd>
  
### Manually:

  Download the [latest release](https://github.com/Feddericokz/GPT-Assistant/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


## Getting started

In order to start using the plugin, you need to give it a OpenIA API key. You can get yours heading over to
https://platform.openai.com/api-keys.

Once you have the API key, head over to <kbd>Settings</kbd> > <kbd>Tools</kbd> > <kbd>GPT Assistant Settings</kbd> 
and set your API key.

![Set API ley](images/set_api_key.gif)

> [!WARNING]  
> Notice how I have to close the settings window and re-open it for the API to kick in, hopefully will be fixed in a later
> release.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
