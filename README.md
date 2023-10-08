# Displayer

**Displayer** is a plugin for managing Display entities in your Minecraft server.

## Features

- Create displays with various types, including items and blocks.
- Manage display properties such as rotation, position, and size.
- List nearby displays and select them for editing.
- Open a user-friendly graphical user interface (GUI) for easy display management.
- And more!

## Usage

- **/display gui**: Open a graphical user interface for display management
- **/display help**: Display plugin help
<\br><\br>
- **/display create \<item | block\> [atSelected]**: Create a new display of the specified type, optionally at a previously selected display
- **/display destroy [nearby] [maxCount] [radius]**: Destroy display(s)
<\br><\br>
- **/display closest**: Select the closest display
- **/display nearby [radius]**: List nearby displays
<\br><\br>
- **/display setrotation \<yaw\> \<pitch\> [roll]**: Set the rotation for the selected display
- **/display changerotation \<yawOffset\> \<pitchOffset\> [rollOffset]**: Change the rotation for the selected display
- **/display setposition \<x\> \<y\> \<z\>**: Set the position for the selected display
- **/display changeposition \<xOffset\> \<yOffset\> \<zOffset\>**: Change the position for the selected display
- **/display setsize \<size\>**: Set the size for the selected display
- **/display changesize \<sizeOffset\>**: Change the size for the selected display

- **/display select \<index\>**: Select a display by its index. This should not be used by the player, and is used in the /display nearby command

## License

This project is licensed under the MIT License. See .\LICENSE for details.
