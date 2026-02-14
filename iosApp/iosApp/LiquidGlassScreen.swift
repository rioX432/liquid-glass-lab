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

enum LiquidGlassPattern: String, CaseIterable {
    case appBarBottomBar = "NavBar + Bar"
    case floatingCard = "Card"
    case fullScreenOverlay = "Overlay"
}

struct LiquidGlassScreen: View {
    @State private var selectedPattern: LiquidGlassPattern = .appBarBottomBar

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Picker("Pattern", selection: $selectedPattern) {
                    ForEach(LiquidGlassPattern.allCases, id: \.self) { pattern in
                        Text(pattern.rawValue).tag(pattern)
                    }
                }
                .pickerStyle(.segmented)
                .padding()

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
            AsyncImage(url: URL(string: "https://picsum.photos/seed/hero/800/1200")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .ignoresSafeArea()
            } placeholder: {
                Color.gray.opacity(0.3)
                    .ignoresSafeArea()
            }

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
            AsyncImage(url: URL(string: "https://picsum.photos/seed/hero/800/1200")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .ignoresSafeArea()
            } placeholder: {
                Color.gray.opacity(0.3)
                    .ignoresSafeArea()
            }

            GlassEffectContainer {
                Color.clear
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .glassEffect(.clear, in: Rectangle())
            }
            .ignoresSafeArea()

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

#Preview {
    LiquidGlassScreen()
}
