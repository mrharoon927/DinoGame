# 🦕 Dino Game – T-Rex Runner

A faithful recreation of the classic Chrome offline dinosaur game built entirely with **Java Swing**. Run, jump, and duck your way through an endless desert with smooth 60 fps gameplay, a parallax day/night cycle, and progressively increasing difficulty.

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Swing](https://img.shields.io/badge/UI-Java%20Swing-blue.svg)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

![Dino Game Preview](assets/DinoGame_Preview.gif)

---

## 🧠 Why This Project?

Most browser-based Dino clones rely on JavaScript or game engines. This one is built **purely in Java with no external libraries** — a clean demonstration of object-oriented design, Swing rendering, and real-time game-loop architecture using only the standard JDK.

---

## 🚀 Key Features

### 🎮 Core Gameplay
- **Jump & Duck**: Classic T-Rex controls — Space/↑ to jump, ↓ to duck
- **Collision Detection**: Pixel-accurate hitbox intersection for obstacles and the dino
- **Progressive Speed**: Game gradually accelerates every 200 score points, capped at 16 px/tick

### 🌗 Day / Night Cycle
- **Automatic toggle**: Scene switches between day and night every 700 score points
- **Parallax clouds**: Clouds scroll at 1/3 game speed for a depth illusion
- **Adaptive colours**: Sky, ground, text, and HUD all shift palette with the cycle

### 📊 HUD & Scoring
- **Live score**: Increments every game tick in a zero-padded 5-digit display
- **Session high score**: Best score preserved in memory for the session lifetime
- **Speed readout**: Current scroll speed shown in the top-left corner
- **Day/Night badge**: Emoji badge updates alongside the cycle toggle

### 🪨 Obstacles
- **Cacti variants**: Single, double, and triple cactus clusters spawned randomly
- **Pterodactyls**: Flying obstacles at varying heights for mid-air challenges
- **Randomised gaps**: Spacing between obstacles scales with game speed

---

## 🛠️ Tech Stack

- **Java 8+** – Core language; no third-party dependencies required
- **Java Swing / AWT** – Rendering, window management, and input handling
- **javax.swing.Timer** – Drives the 60 fps (~16 ms) game loop
- **java.awt.Graphics2D** – Anti-aliased 2D rendering with gradient fills

---

## 📋 System Requirements

### Prerequisites
- **JDK 8** or higher
- Any Java IDE (IntelliJ IDEA, Eclipse, VS Code with Java extension) **or** a terminal

### Verify Your Setup
```bash
# Check Java version (should be 8+)
java -version

# Check compiler
javac -version
```

---

## ⚡ Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/AnkeshGG/DinoGame.git
cd DinoGame
```

### 2. Compile the Source
```bash
javac -d out src/*.java
```

### 3. Run the Game
```bash
java -cp out DinoGame
```

> **IntelliJ IDEA**: Open the project, right-click `DinoGame.java` → *Run 'DinoGame.main()'*

---

## 🎮 Controls

| Key | Action |
|-----|--------|
| `Space` / `↑` | Jump (also starts / restarts the game) |
| `↓` | Duck (hold to crouch, release to stand) |
| `Enter` | Restart after Game Over |

---

## 🏗️ Project Structure

```
dino-game/
├── src/
│   ├── DinoGame.java          # Main panel, game loop, rendering, input
│   ├── Dino.java              # Player character: physics, animation, hitbox
│   ├── ObstacleGenerator.java # Spawns, updates, and draws obstacles
│   ├── Cloud.java             # Decorative parallax background clouds
│   └── GameState.java         # Enum: IDLE | RUNNING | GAME_OVER
├── assets/
│   └── DinoGame_Preview.gif   # Gameplay demo clip
├── .gitignore
└── README.md
```

---

## 🧩 Architecture Overview

The game follows a simple **component-based architecture** driven by a single Swing `Timer`:

- **`DinoGame`** — Central hub: owns the game loop (`actionPerformed`), manages all entities, handles rendering back-to-front, and processes keyboard input.
- **`Dino`** — Self-contained player entity with jump physics (gravity + velocity), duck state, leg animation, and a shrinkable hitbox.
- **`ObstacleGenerator`** — Stateless spawner that maintains a list of active `Obstacle` objects; each obstacle knows its own type, position, and how to draw itself.
- **`Cloud`** — Lightweight value object; scrolls left at reduced speed and is recycled when off-screen.
- **`GameState`** — Three-value enum (`IDLE`, `RUNNING`, `GAME_OVER`) that gates all logic branches cleanly.

---

## 🧪 Testing

> Automated unit tests are planned for a future release. Current validation is done through manual playtesting across all three game states and both day/night modes.

---

## 💡 Future Enhancements

- [ ] Add sound effects (jump, game over, milestone)
- [ ] Persist high score to a local file across sessions
- [ ] Add a settings panel (volume, key remapping)
- [ ] Mobile-style touch/click controls
- [ ] Difficulty presets (Easy / Normal / Hard)

---

## 🤝 Contributing

You're welcome to contribute to this open-source project!

### Steps:
1. Fork the repo
2. Create a branch: `git checkout -b feature/newFeature`
3. Commit your changes
4. Push: `git push origin feature/newFeature`
5. Submit a pull request

### Guidelines
- Follow standard Java code conventions
- Keep all rendering logic inside the relevant class's `draw()` method
- Test across both day and night modes before submitting PRs

---

## 📄 License

This project is licensed under the **MIT License** – see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 About the Author

**Ankesh Kumar** – *Java Developer & Game Enthusiast*

> I built this project to deepen my understanding of real-time rendering and game-loop design using nothing but the standard Java library — no engines, no frameworks, just pure Swing.

### Connect With Me
- 🌐 **GitHub**: [@AnkeshGG](https://github.com/AnkeshGG)
- 💼 **LinkedIn**: [Ankesh Kumar](https://www.linkedin.com/in/ankeshgg/)
- 🔗 **Medium**: [ankeshGG](https://medium.com/@ankeshGG)

---

## 🙏 Acknowledgements

- **Chrome Dino** – The original game by Google that inspired this recreation
- **Java Swing Team** – For a surprisingly capable 2D rendering toolkit
- **Open Source Community** – For countless references and inspiration

---

## 📊 Project Status

🟢 Actively maintained

- **Version**: 1.0.0  
- **Last Updated**: February 2026  
- **Stars**: ⭐ Give this project a star if you enjoyed it!
- **Issues**: Tracked via GitHub Issues

---

### 🔍 Ready to Beat Your High Score?

An endless runner with no finish line — just you, a dinosaur, and an ever-faster desert. Jump over cacti, dodge pterodactyls, and push through the night to set a new personal best.

**[📥 Clone & Play](#-quick-start)** | **[🎮 Controls](#-controls)** | **[🤝 Contribute](#-contributing)**

---
© 2026 Ankesh Kumar.

*Keep running — the desert never ends.*  
![Built with love](https://img.shields.io/badge/Built%20with-%E2%9D%A4-red)