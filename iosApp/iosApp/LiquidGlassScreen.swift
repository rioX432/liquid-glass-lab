import SwiftUI

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
            Tab("Glass", systemImage: "sparkle", value: 0) {
                GlassEffectDemoView()
            }
            Tab("Cards", systemImage: "rectangle.on.rectangle", value: 1) {
                GlassCardDemoView()
            }
            Tab("Interactive", systemImage: "hand.tap", value: 2) {
                InteractiveGlassDemoView()
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
