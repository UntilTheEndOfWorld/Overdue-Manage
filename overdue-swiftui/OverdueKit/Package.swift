// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "OverdueKit",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(name: "OverdueKit", targets: ["OverdueKit"])
    ],
    targets: [
        .target(
            name: "OverdueKit",
            path: "Sources/OverdueKit"
        )
    ]
)
