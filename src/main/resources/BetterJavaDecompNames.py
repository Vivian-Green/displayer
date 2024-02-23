import sys
import re

java_primitive_types = [('byte', ''), ('short', ''), ('int', ''), ('long', ''), ('float', ''), ('double', ''), ('boolean', ''), ('char', '')]


def replaceSymbolWithEdgesWithSymbolWithEdges(content, symbol, replacement, L, R):
    newSymbol = L + symbol + R
    newSymbolReplacement = L + replacement + R
    return content.replace(newSymbol, newSymbolReplacement)

def print_decomped_java_with_better_names(path):
    print("\n\n")

    symbolsWithCounts = {}

    with open(path, 'r') as file:
        oldContent = file.read()

    # Find all UpperCamelCase words followed by a space and then a lowerCamelCase or p_ word
    matches = re.findall(r'([A-Z][a-zA-Z0-9_]*)(?=\s*([a-z][a-zA-Z0-9_]*|p_\w+))', oldContent)

    matches = list(set(matches))  # deduplicate

    print(matches)

    content = oldContent

    for typeName in matches:
        upperCamelCase, followingWord = typeName

        # Symbol's count
        symbolsWithCounts[followingWord] = symbolsWithCounts.get(followingWord, 0) + 1

        count = str(symbolsWithCounts[followingWord])
        if count == "1":
            count = ""

        # Replace
        newSymbol = upperCamelCase[0].lower() + upperCamelCase[1:] + count

        # Append the count to newSymbolReplacement
        newSymbolReplacement = newSymbol + count

        content = re.sub(r'\b' + re.escape(followingWord) + r'\b', newSymbolReplacement, content)

    # Loop through primitive types
    primitive_types = [('byte', ''), ('short', ''), ('int', ''), ('long', ''), ('float', ''), ('double', ''), ('boolean', ''), ('char', '')]

    for typeName, _ in primitive_types:
        # Find all occurrences of symbols following the primitive type
        symbols = re.findall(r'\b{}\s+([a-zA-Z_][a-zA-Z0-9_]*)\b'.format(typeName), content)

        count = 0;

        # Replace each symbol
        for symbol in symbols:
            count += 1

            if not "p_" in symbol:
                continue

            content = replaceSymbolWithEdgesWithSymbolWithEdges(content, symbol, typeName + str(count), "", "")
            #content = replaceSymbolWithEdgesWithSymbolWithEdges(content, symbol, typeName + str(count), " ", ")")
            #content = replaceSymbolWithEdgesWithSymbolWithEdges(content, symbol, typeName + str(count), "(", " ")
            #content = replaceSymbolWithEdgesWithSymbolWithEdges(content, symbol, typeName + str(count), "(", ")")
            #content = replaceSymbolWithEdgesWithSymbolWithEdges(content, symbol, typeName + str(count), " ", ",")
            #content = replaceSymbolWithEdgesWithSymbolWithEdges(content, symbol, typeName + str(count), ",", ",")


    with open(path.replace('.java', '.renamed.java'), 'w') as file:
        file.write(content)


if __name__ == "__main__":
    # todo: time this

    if len(sys.argv) < 2:
        print("Please drag and drop a java file onto this script.")
    else:
        file_path = sys.argv[1]
        if ".java" in file_path:
            print_decomped_java_with_better_names(file_path)
            holdMe = input("\n\ndone! press enter to exit")
        else:
            print("Please drag and drop a java file onto this script.")