import Foundation

extension NSError {
    convenience init(description: String, code: Int = 0) {
        self.init(
            domain: "plist script",
            code: code,
            userInfo: [NSLocalizedDescriptionKey: description]
        )
    }
}

let inputPlistURL = URL(
    fileURLWithPath: CommandLine.arguments[1]
)
let outputPlistURL = URL(
    fileURLWithPath: CommandLine.arguments[2]
)

try [inputPlistURL,
 outputPlistURL
].forEach { url in
    guard url.pathExtension == "plist" else
    { throw NSError(description: "invalid argument: \(url)") }
}
guard inputPlistURL != outputPlistURL else
{ throw NSError(description: "This will override input file!") }

let plist = try PropertyListSerialization.propertyList(
    from: try Data(contentsOf: inputPlistURL),
    format: nil
)

func replacePlistEnvironmentVariables(_ plist: Any) throws -> Any {
    switch plist {
    case let dictionary as [String: Any]:
        return try dictionary.mapValues(replacePlistEnvironmentVariables)
    case let array as [Any]:
        return try array.map(replacePlistEnvironmentVariables)
        
    case let string as String:
        return try NSRegularExpression(
            pattern: "\\$\\((\\w+)\\)"
        ).matches(
            in: string,
            range: NSRange(
                string.startIndex..<string.endIndex,
                in: string
            )
        ).reversed().reduce(into: string) { result, match in
            let replaceRange = Range(
                match.range(at: 0),
                in: string
            )!
            let variableRange = Range(
                match.range(at: 1),
                in: string
            )!
            let variable = String(string[variableRange])
            result.replaceSubrange(
                replaceRange,
                with: ProcessInfo.processInfo.environment[variable]!
            )
        }
        
    default: return plist
    }
}

let resultPlist = try replacePlistEnvironmentVariables(plist)

try PropertyListSerialization.data(
    fromPropertyList: resultPlist,
    format: .xml,
    options: .zero
).write(to: outputPlistURL)
