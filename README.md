todo: review readme & ensure up to date

# Displayer

**Displayer** is a spigot plugin for managing Display entities in your Minecraft server.

## Features

- Create displays with various types, including items and blocks.
- Manage display properties such as rotation, position, and size.
- List nearby displays and select them for editing.
- Open a user-friendly graphical user interface (GUI) for easy, precise display management.
- in-game tools for faster display editing

## Startup Guide

## Step 1: Create a Display

1. **Position Yourself:** Stand (and face) in the location where you want the display to appear, and hold the item you wish to display.

2. **Execute Command:** Use the command `/display create [item | block]`. You should see a display right where your head is, if you back up.

## Step 2: Manipulate it with the display tools

Once the display is created, use the following items to manipulate it: (these will probably be moved to a config file at some point)
- **Lead:** movement tool, for movement and yaw rotation. Move your player while holding it to carry your display alongside. Rotating left and right does the same to your display. 
- **Spectral Arrow:** rotation tool, for pitch and roll rotations. Turn your camera to rotate your display.
- **Blaze Rod:** scaling tool. move in a positive direction to enlarge your display, or in a negative direction to shrink it.

## Additional Tips

- **GUI Interface:** Use `/display gui` to access a gui for precise adjustments to position, rotation, and size.

- **Display Groups:** Experiment with `/displaygroup` commands to group displays together for coordinated manipulation. This needs more details.

- **Destroy Command:** In case of mistakes, you can use `/display destroy` to remove a display or group of displays.
## Usage

### General Commands:

- **/display create \<item | block\> [atSelected]**: Create a new display of the specified type, optionally at a previously selected display.
  - Example: `/display create`
  - Example 2: `/display create block atSelected`

- **/display destroy [\<nearby\> \<maxCount\> \<radius\>]**: Destroy display(s).
  - Example: `/display destroy`
  - Example 2: `/display destroy nearby 5`

- **/display closest [\<radius\>]**: Select the closest display.
  - Example: `/display closest`
  - Example 2: `/display closest 10`

- **/display nearby [\<radius\>]**: List nearby displays.
  - Example: `/display nearby`
  - Example 2: `/display nearby 15`

- **/display gui**: Open a graphical user interface for display management.
  - Example: `/display gui`

- **/display help**: Display plugin help.
  - Example: `/display help`

### Advanced Display Commands:

- **/advdisplay setrotation \<yaw> \<pitch> [\<roll\>]**: Set the rotation for the selected display.
  - Example: `/advdisplay setrotation 90 45`

- **/advdisplay changerotation \<yawOffset> \<pitchOffset> [\<rollOffset\>]**: Change the rotation for the selected display.
  - Example: `/advdisplay changerotation 5 10`

- **/advdisplay setposition \<x> \<y> \<z>**: Set the position for the selected display.
  - Example: `/advdisplay setposition 10 20 30`

- **/advdisplay changeposition \<xOffset> \<yOffset> \<zOffset>**: Change the position for the selected display.
  - Example: `/advdisplay changeposition -2 0 5`

- **/advdisplay setsize \<size>**: Set the size for the selected display.
  - Example: `/advdisplay setsize 2`

- **/advdisplay changesize \<sizeOffset>**: Change the size for the selected display.
  - Example: `/advdisplay changesize -0.5`

- **/advdisplay rename \<name>**: Set the name for a given display.
  - Example: `/advdisplay rename DisplayeyMcDisplayFace`

- **/advdisplay details**: Get details about the selected display.
  - Example: `/advdisplay details`

### Display Group Commands:

- **/displaygroup parent \<parentName>**: Sets the parent of the selected display.
  - Example: `/displaygroup parent TheTableYouSetThisLampDisplayOn`

- **/displaygroup unparent**: Unsets the parent of the selected display.
  - Example: `/displaygroup unparent`

- **/displaygroup copypaste**: Copies and pastes the hierarchy of the selected display at the player's current location. Not functional at the time of writing this.
  - Example: `/displaygroup copypaste`

- **/displaygroup rotate \<xRotation> \<yRotation> \<zRotation>**: Rotates the entire group for the selected display.
  - Example: `/displaygroup rotate 0 90 0`

- **/displaygroup translate \<xTranslation> \<yTranslation> \<zTranslation>**: Translates the entire group for the selected display.
  - Example: `/displaygroup translate 5 0 -3`

- **/displaygroup show**: Highlights the display group selected.
  - Example: `/displaygroup show`
    
## License

This project is licensed under the MIT License. See .\LICENSE for details.
