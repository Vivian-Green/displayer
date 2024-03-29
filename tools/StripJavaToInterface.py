import sys
import re
import os

package_name = "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"

def remove_comments_and_strings_from_content(content):
    new_content = ""
    inside_block_comment = False
    new_lines = []
    for line in content.split('\n'):
        stripped_line = line.strip()

        # Remove single line comments
        if "//" in stripped_line:
            stripped_line = stripped_line[:stripped_line.index("//")]

        # Remove block comments
        if "/*" in stripped_line:
            inside_block_comment = True

        if inside_block_comment:
            if "*/" in stripped_line:
                inside_block_comment = False
                end_index = stripped_line.index("*/") + 2
                if end_index < len(stripped_line):
                    new_content += '\n' + stripped_line[end_index:]
                stripped_line = stripped_line[stripped_line.index("*/")+2:] + '\n'
            else:
                if "/*" in stripped_line:
                    stripped_line = stripped_line[:stripped_line.index("/*")]
                else:
                    stripped_line = ""

        if ("if (" in stripped_line or "if(" in stripped_line):
            if "{" in stripped_line:
                if ("} else" in stripped_line):
                    stripped_line = "}{"
                else:
                    stripped_line = "{"

        # Remove strings
        stripped_line = re.sub(r"\".*?\"", "", stripped_line)
        stripped_line = re.sub(r"\'.*?\'", "", stripped_line)

        new_lines.append(stripped_line)
    return new_lines




def is_start_of_java_declaration(line):
    stripped_line = line.strip()

    # will catch most of 'em
    if (stripped_line.startswith("public ") or stripped_line.startswith("private ") or stripped_line.startswith("protected ") or "static" in stripped_line) and "(" in stripped_line:
        #print("AAAAA: "+stripped_line)
        return True
    return False


def is_end_of_java_declaration(line):
    stripped_line = line.strip()
    if ")" in stripped_line and "{" in stripped_line:
        #print("BBBBB: " + stripped_line)
        return stripped_line.index("{")
    return -1

def print_java_declarations(path):
    global package_name

    print("\n\n")
    try:
        with open(path, 'r') as file:
            content = file.read()
    except FileNotFoundError:
        print(f"File not found: {path}")
        return
    except Exception as e:
        print(f"An error occurred: {e}")
        return

    lines = remove_comments_and_strings_from_content(content)

    inside_declaration = False
    declaration = "\n"

    nesting = 0

    classEntryNesting = -1

    newLines = []

    for line in lines:
        if "package" in line.lower() and len(package_name) > len(line[8:-1]):
            package_name = line[8:-1]


        if "class" in line.lower():
            classEntryNesting = nesting
            if "{" in line:
                thisSignature = line[:line.index("{") + 1]
                print(thisSignature)
                newLines.append(thisSignature + "\n")

        # print(line)
        if is_start_of_java_declaration(line):
            inside_declaration = True
            declaration = line.strip()
        if inside_declaration:
            declaration += line.strip()
            end_index = is_end_of_java_declaration(line)
            if end_index != -1:
                declaration = declaration[:end_index + 1]
                thisSignature = "    " * nesting + declaration + "}"
                print(thisSignature)
                newLines.append(thisSignature + "\n")
                inside_declaration = False
                declaration = ""

        nesting += line.count('{') - line.count('}')

        # print(" "*nesting + str(nesting))

        if classEntryNesting > nesting and "}" in line:
            classEntryNesting = -1
            print("}")
            newLines.append("}")


    with open(path.replace('.java', '.interface.txt'), 'w') as file:
        file.writelines(newLines)

    print("\n\n")

    return path.replace('.java', '.interface.txt')


if __name__ == "__main__":
    #print_java_declarations("ArmorStand.java")
    #sys.argv = ["e", "GUIHandler.java", "NBTMagic.java"]

    interfacePaths = []

    if len(sys.argv) < 2:
        print("Please drag and drop a file onto this script.")
        holdMe = input("\n\npress enter to exit")
        exit()
    else:
        for i in range(len(sys.argv)-1):
            file_path = sys.argv[i+1]
            interfacePaths.append(print_java_declarations(file_path))

    interfacesStr = ""

    for path in interfacePaths:
        with open(path, 'r') as file:
            content = file.read()

            interfacesStr += content + "\n\n\n\n\n"
        os.remove(path)


    if len(sys.argv) > 2:
        with open(package_name + ".interfaces.txt", 'w') as file:
            file.writelines(interfacesStr)

    holdMe = input("\n\npress enter to exit")
    exit()
