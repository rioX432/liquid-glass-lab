import SwiftUI

struct LiquidGlassScreen: View {
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

// MARK: - Glass Effect Demo

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

// MARK: - Glass Card Demo

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

// MARK: - Interactive Glass Demo

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
