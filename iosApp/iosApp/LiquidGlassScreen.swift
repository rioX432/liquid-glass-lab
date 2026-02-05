import SwiftUI

// MARK: - Glass Effect Container

struct GlassEffectContainer<Content: View>: View {
    let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        content
    }
}

// MARK: - Main Screen

struct LiquidGlassScreen: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            Tab("Playground", systemImage: "sparkle", value: 0) {
                PlaygroundView()
            }
            Tab("Comparison", systemImage: "rectangle.on.rectangle", value: 1) {
                ComparisonView()
            }
        }
    }
}

// MARK: - Playground View

struct PlaygroundView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            Tab("Custom", systemImage: "slider.horizontal.3", value: 0) {
                CustomControlsView()
            }
            Tab("Glass", systemImage: "sparkle", value: 1) {
                GlassEffectDemoView()
            }
            Tab("Cards", systemImage: "rectangle.on.rectangle", value: 2) {
                GlassCardDemoView()
            }
            Tab("Interactive", systemImage: "hand.tap", value: 3) {
                InteractiveGlassDemoView()
            }
        }
    }
}

// MARK: - Custom Controls View

enum GlassStyleType: String, CaseIterable {
    case regular = "Regular"
    case clear = "Clear"
}

enum TintColorOption: String, CaseIterable {
    case none = "None"
    case blue = "Blue"
    case red = "Red"
    case green = "Green"
    case purple = "Purple"
    case orange = "Orange"

    var color: Color? {
        switch self {
        case .none: return nil
        case .blue: return .blue
        case .red: return .red
        case .green: return .green
        case .purple: return .purple
        case .orange: return .orange
        }
    }
}

struct CustomControlsView: View {
    @State private var glassStyle: GlassStyleType = .regular
    @State private var tintColor: TintColorOption = .none
    @State private var tintOpacity: Double = 0.5
    @State private var isInteractive: Bool = false
    @State private var cornerRadius: Double = 24

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Preview section
                    ZStack {
                        AsyncImage(url: URL(string: "https://picsum.photos/seed/custom/800/600")) { image in
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Color.gray.opacity(0.3)
                        }
                        .frame(height: 300)
                        .clipped()

                        // Glass effect preview
                        GlassPreviewContent(
                            glassStyle: glassStyle,
                            tintColor: tintColor,
                            tintOpacity: tintOpacity,
                            isInteractive: isInteractive,
                            cornerRadius: cornerRadius
                        )
                    }
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    // Controls
                    VStack(alignment: .leading, spacing: 16) {
                        // Glass Style
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Glass Style")
                                .font(.headline)
                            Picker("Style", selection: $glassStyle) {
                                ForEach(GlassStyleType.allCases, id: \.self) { style in
                                    Text(style.rawValue).tag(style)
                                }
                            }
                            .pickerStyle(.segmented)
                        }

                        Divider()

                        // Tint Color
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Tint Color")
                                .font(.headline)
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    ForEach(TintColorOption.allCases, id: \.self) { option in
                                        Button {
                                            tintColor = option
                                        } label: {
                                            Circle()
                                                .fill(option.color ?? Color.gray.opacity(0.3))
                                                .frame(width: 44, height: 44)
                                                .overlay {
                                                    if option == .none {
                                                        Image(systemName: "xmark")
                                                            .foregroundStyle(.secondary)
                                                    }
                                                }
                                                .overlay {
                                                    if tintColor == option {
                                                        Circle()
                                                            .strokeBorder(Color.primary, lineWidth: 3)
                                                    }
                                                }
                                        }
                                        .buttonStyle(.plain)
                                    }
                                }
                            }

                            if tintColor != .none {
                                HStack {
                                    Text("Opacity")
                                    Slider(value: $tintOpacity, in: 0.1...1.0)
                                    Text("\(Int(tintOpacity * 100))%")
                                        .frame(width: 50)
                                }
                            }
                        }

                        Divider()

                        // Corner Radius
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Corner Radius")
                                .font(.headline)
                            HStack {
                                Slider(value: $cornerRadius, in: 0...100)
                                Text("\(Int(cornerRadius))")
                                    .frame(width: 40)
                            }
                        }

                        Divider()

                        // Interactive Toggle
                        Toggle("Interactive Mode", isOn: $isInteractive)
                            .font(.headline)

                        // Current settings summary
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Current Settings")
                                .font(.headline)
                            Text("Style: \(glassStyle.rawValue)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            Text("Tint: \(tintColor.rawValue)\(tintColor != .none ? " @ \(Int(tintOpacity * 100))%" : "")")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            Text("Corner: \(Int(cornerRadius))pt | Interactive: \(isInteractive ? "Yes" : "No")")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        .padding()
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.gray.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .padding(.horizontal)
                }
                .padding(.vertical)
            }
            .navigationTitle("Custom Controls")
        }
    }
}

struct GlassPreviewContent: View {
    let glassStyle: GlassStyleType
    let tintColor: TintColorOption
    let tintOpacity: Double
    let isInteractive: Bool
    let cornerRadius: Double

    var body: some View {
        let content = VStack(spacing: 8) {
            Text("Glass Preview")
                .font(.headline)
            Text("Style: \(glassStyle.rawValue)")
                .font(.caption)
            if tintColor != .none {
                Text("Tint: \(tintColor.rawValue) (\(Int(tintOpacity * 100))%)")
                    .font(.caption)
            }
            if isInteractive {
                Text("Interactive: ON")
                    .font(.caption)
            }
        }
        .padding(20)

        let shape = RoundedRectangle(cornerRadius: cornerRadius)

        // Apply glass effect based on settings
        Group {
            switch (glassStyle, tintColor, isInteractive) {
            // Regular style combinations
            case (.regular, .none, false):
                content.glassEffect(.regular, in: shape)
            case (.regular, .none, true):
                content.glassEffect(.regular.interactive(), in: shape)
            case (.regular, let tint, false) where tint != .none:
                content.glassEffect(.regular.tint(tint.color!.opacity(tintOpacity)), in: shape)
            case (.regular, let tint, true) where tint != .none:
                content.glassEffect(.regular.tint(tint.color!.opacity(tintOpacity)).interactive(), in: shape)

            // Clear style combinations
            case (.clear, .none, false):
                content.glassEffect(.clear, in: shape)
            case (.clear, .none, true):
                content.glassEffect(.clear.interactive(), in: shape)
            case (.clear, let tint, false) where tint != .none:
                content.glassEffect(.clear.tint(tint.color!.opacity(tintOpacity)), in: shape)
            case (.clear, let tint, true) where tint != .none:
                content.glassEffect(.clear.tint(tint.color!.opacity(tintOpacity)).interactive(), in: shape)

            default:
                content.glassEffect(.regular, in: shape)
            }
        }
    }
}

// MARK: - Comparison View

enum ComparisonPattern: String, CaseIterable {
    case appBarBottomBar = "AppBar + BottomBar"
    case floatingCard = "Floating Card"
    case fullScreenOverlay = "Full Screen Overlay"
}

struct ComparisonView: View {
    @State private var selectedPattern: ComparisonPattern = .appBarBottomBar

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Pattern selector
                Picker("Pattern", selection: $selectedPattern) {
                    ForEach(ComparisonPattern.allCases, id: \.self) { pattern in
                        Text(pattern.rawValue).tag(pattern)
                    }
                }
                .pickerStyle(.segmented)
                .padding()

                // Pattern content
                Group {
                    switch selectedPattern {
                    case .appBarBottomBar:
                        LiquidGlassAppBarBottomBarPattern()
                    case .floatingCard:
                        LiquidGlassFloatingCardPattern()
                    case .fullScreenOverlay:
                        LiquidGlassFullScreenOverlayPattern()
                    }
                }
            }
            .navigationTitle("iOS Liquid Glass")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

// MARK: - Pattern 1: AppBar + Content + BottomBar

struct LiquidGlassAppBarBottomBarPattern: View {
    var body: some View {
        ZStack {
            // Content: image grid
            ScrollView {
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                    ForEach(1...12, id: \.self) { index in
                        AsyncImage(url: URL(string: "https://picsum.photos/seed/lg\(index)/400/300")) { image in
                            image
                                .resizable()
                                .aspectRatio(4/3, contentMode: .fill)
                        } placeholder: {
                            Color.gray.opacity(0.3)
                                .aspectRatio(4/3, contentMode: .fill)
                        }
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                }
                .padding(8)
                .padding(.top, 60)
                .padding(.bottom, 80)
            }

            VStack {
                // Top bar with glass effect
                GlassEffectContainer {
                    HStack {
                        Text("Gallery")
                            .font(.headline)
                        Spacer()
                        Image(systemName: "magnifyingglass")
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .glassEffect(.regular, in: RoundedRectangle(cornerRadius: 0))
                }
                .frame(maxWidth: .infinity)

                Spacer()

                // Bottom bar with glass effect
                GlassEffectContainer {
                    HStack(spacing: 0) {
                        ForEach(
                            ["house.fill", "magnifyingglass", "heart.fill", "person.fill"],
                            id: \.self
                        ) { icon in
                            Button { } label: {
                                Image(systemName: icon)
                                    .font(.title2)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 12)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .glassEffect(.regular, in: RoundedRectangle(cornerRadius: 0))
                }
                .frame(maxWidth: .infinity)
            }
        }
    }
}

// MARK: - Pattern 2: Floating Card over Image

struct LiquidGlassFloatingCardPattern: View {
    var body: some View {
        ZStack {
            // Background image
            AsyncImage(url: URL(string: "https://picsum.photos/seed/hero/800/1200")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .ignoresSafeArea()
            } placeholder: {
                Color.gray.opacity(0.3)
                    .ignoresSafeArea()
            }

            // Floating card with glass effect
            GlassEffectContainer {
                VStack(alignment: .leading, spacing: 12) {
                    Text("Floating Card")
                        .font(.title2)
                        .fontWeight(.semibold)

                    Text("This card floats over a background image with a blur effect applied using iOS Liquid Glass.")
                        .font(.body)
                        .foregroundStyle(.secondary)

                    Text("The content behind the card is blurred, creating a frosted glass appearance.")
                        .font(.caption)
                        .foregroundStyle(.tertiary)
                }
                .padding(24)
                .frame(maxWidth: .infinity, alignment: .leading)
                .glassEffect(.regular, in: RoundedRectangle(cornerRadius: 24))
            }
            .padding(24)
        }
    }
}

// MARK: - Pattern 3: Full Screen Blur Overlay

struct LiquidGlassFullScreenOverlayPattern: View {
    var body: some View {
        ZStack {
            // Background image
            AsyncImage(url: URL(string: "https://picsum.photos/seed/hero/800/1200")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .ignoresSafeArea()
            } placeholder: {
                Color.gray.opacity(0.3)
                    .ignoresSafeArea()
            }

            // Full screen blur overlay with modal
            GlassEffectContainer {
                Color.clear
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .glassEffect(.clear, in: Rectangle())
            }
            .ignoresSafeArea()

            // Modal content
            VStack(spacing: 16) {
                Text("Confirm Action")
                    .font(.title2)
                    .fontWeight(.semibold)

                Text("This modal demonstrates a full-screen blur overlay using iOS Liquid Glass. The background content is blurred behind this dialog.")
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)

                HStack(spacing: 12) {
                    Button {
                    } label: {
                        HStack {
                            Image(systemName: "xmark")
                            Text("Cancel")
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .glassEffect(.regular.interactive(), in: RoundedRectangle(cornerRadius: 12))
                    }
                    .buttonStyle(.plain)

                    Button {
                    } label: {
                        HStack {
                            Image(systemName: "checkmark")
                            Text("Confirm")
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .glassEffect(.regular.tint(.blue).interactive(), in: RoundedRectangle(cornerRadius: 12))
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(24)
            .background {
                RoundedRectangle(cornerRadius: 24)
                    .fill(.regularMaterial)
            }
            .padding(32)
        }
    }
}

// MARK: - Glass Effect Demo (Playground)

struct GlassEffectDemoView: View {
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Hero image with glass overlay
                    ZStack(alignment: .bottom) {
                        AsyncImage(url: URL(string: "https://picsum.photos/seed/hero/800/600")) { image in
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Color.gray.opacity(0.3)
                        }
                        .frame(height: 300)
                        .clipped()

                        // Glass variants
                        VStack(spacing: 12) {
                            Text("Regular Glass")
                                .font(.headline)
                                .padding(.horizontal, 20)
                                .padding(.vertical, 10)
                                .glassEffect(.regular, in: .capsule)

                            Text("Clear Glass")
                                .font(.headline)
                                .padding(.horizontal, 20)
                                .padding(.vertical, 10)
                                .glassEffect(.clear, in: .capsule)
                        }
                        .padding(.bottom, 20)
                    }
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    // Shape variations
                    Text("Shape Variations")
                        .font(.title3)
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                        GlassShapeDemo(label: "Capsule", shape: .capsule)
                        GlassShapeDemo(label: "Circle", shape: .circle)
                        GlassShapeDemo(label: "Rounded Rect", shape: RoundedRectangle(cornerRadius: 12))
                        GlassShapeDemo(label: "Ellipse", shape: .ellipse)
                    }

                    // Tinted glass
                    Text("Tinted Glass")
                        .font(.title3)
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    HStack(spacing: 12) {
                        ForEach(
                            [Color.blue, Color.red, Color.green, Color.purple],
                            id: \.self
                        ) { color in
                            Image(systemName: "star.fill")
                                .font(.title2)
                                .padding(12)
                                .glassEffect(.regular.tint(color), in: .circle)
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Glass Effects")
        }
    }
}

struct GlassShapeDemo<S: Shape>: View {
    let label: String
    let shape: S

    var body: some View {
        ZStack {
            AsyncImage(url: URL(string: "https://picsum.photos/seed/\(label)/400/400")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.gray.opacity(0.3)
            }
            .frame(height: 120)
            .clipped()

            Text(label)
                .font(.caption)
                .fontWeight(.medium)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .glassEffect(.regular, in: shape)
        }
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// MARK: - Glass Card Demo (Playground)

struct GlassCardDemoView: View {
    var body: some View {
        NavigationStack {
            ZStack {
                // Background image grid
                ScrollView {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                        ForEach(1...12, id: \.self) { index in
                            AsyncImage(url: URL(string: "https://picsum.photos/seed/lg\(index)/400/300")) { image in
                                image
                                    .resizable()
                                    .aspectRatio(4/3, contentMode: .fill)
                            } placeholder: {
                                Color.gray.opacity(0.3)
                                    .aspectRatio(4/3, contentMode: .fill)
                            }
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }
                    .padding(8)
                    .padding(.bottom, 200)
                }

                // Floating glass cards
                VStack {
                    Spacer()
                    GlassEffectContainer {
                        VStack(spacing: 12) {
                            HStack {
                                Image(systemName: "photo.on.rectangle.angled")
                                    .font(.title2)
                                Text("Gallery")
                                    .font(.headline)
                                Spacer()
                                Text("12 photos")
                                    .font(.subheadline)
                                    .foregroundStyle(.secondary)
                            }
                            .padding(16)
                            .glassEffect(.regular, in: RoundedRectangle(cornerRadius: 16))

                            HStack(spacing: 12) {
                                ForEach(
                                    ["heart.fill", "square.and.arrow.up", "trash"],
                                    id: \.self
                                ) { icon in
                                    Image(systemName: icon)
                                        .font(.title3)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 12)
                                        .glassEffect(.regular.interactive(), in: RoundedRectangle(cornerRadius: 12))
                                }
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Glass Cards")
        }
    }
}

// MARK: - Interactive Glass Demo (Playground)

struct InteractiveGlassDemoView: View {
    @State private var isExpanded = false
    @State private var selectedIndex: Int? = nil
    @Namespace private var glassNamespace

    var body: some View {
        NavigationStack {
            ZStack {
                AsyncImage(url: URL(string: "https://picsum.photos/seed/interactive/800/1200")) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .ignoresSafeArea()
                } placeholder: {
                    Color.gray.opacity(0.3)
                        .ignoresSafeArea()
                }

                VStack {
                    Spacer()

                    GlassEffectContainer {
                        VStack(spacing: 16) {
                            // Morphing glass buttons
                            if isExpanded {
                                VStack(spacing: 8) {
                                    ForEach(0..<4) { index in
                                        Button {
                                            selectedIndex = index
                                        } label: {
                                            HStack {
                                                Image(systemName: actionIcons[index])
                                                Text(actionLabels[index])
                                                Spacer()
                                                if selectedIndex == index {
                                                    Image(systemName: "checkmark")
                                                }
                                            }
                                            .padding(14)
                                            .glassEffect(
                                                selectedIndex == index
                                                    ? .regular.tint(.blue)
                                                    : .regular,
                                                in: RoundedRectangle(cornerRadius: 12)
                                            )
                                            .glassEffectID(index, in: glassNamespace)
                                        }
                                        .buttonStyle(.plain)
                                    }
                                }
                                .transition(.move(edge: .bottom).combined(with: .opacity))
                            }

                            Button {
                                withAnimation(.spring(duration: 0.4)) {
                                    isExpanded.toggle()
                                }
                            } label: {
                                HStack {
                                    Image(systemName: isExpanded ? "xmark" : "plus")
                                    Text(isExpanded ? "Close" : "Actions")
                                }
                                .font(.headline)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 14)
                                .glassEffect(
                                    .regular.interactive(),
                                    in: .capsule
                                )
                            }
                            .buttonStyle(.plain)
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Interactive")
        }
    }

    private var actionIcons: [String] {
        ["heart.fill", "bookmark.fill", "square.and.arrow.up", "ellipsis"]
    }

    private var actionLabels: [String] {
        ["Favorite", "Bookmark", "Share", "More"]
    }
}

#Preview {
    LiquidGlassScreen()
}
