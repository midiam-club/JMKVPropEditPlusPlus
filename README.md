# JMKVPropEdit++ (Vibe Coding Fork)

This project is a sophisticated Java GUI for **mkvpropedit** (part of the MKVToolNix suite), designed to batch edit properties of Matroska files without remuxing.

> [!IMPORTANT]
> **Fork Information**: This is a fork of the original [JMkvpropedit by BrunoReX](https://github.com/BrunoReX/jmkvpropedit).
>
> **Development**: This enhanced version has been developed with **Vibe Coding**.

## 🚀 Key Functional Features

This fork focuses on enhancing usability and providing powerful profile management tools.

### 🌟 Advanced Profile Management
One of the most significant additions in **JMKVPropEdit++** is the comprehensive Profile System, available across all three main tabs: **Video**, **Audio**, and **Subtitles**.

- **Create & Update Profiles**: You can save your frequently used configurations for Video, Audio, or Subtitle tracks as reusable profiles.
- **Easy Assignment**: Simply **double-click** on a profile in the list to instantly apply its settings to the selected track options.
- **Drag & Drop Reordering**: Organize your profiles exactly how you want them by dragging and dropping them in the list.

### ✨ Independent Numbering Option
- **Flexible Control**: The "Numbering" option for tracks is now completely independent. You don't need to enable the "Track Name" to use it.
- **Smart Activation**: Just check "Edit this track" and the numbering options become available immediately.

### 🔄 Integrated Auto-Updater
- **Zero Configuration**: The application includes a built-in downloader. If it doesn't find `mkvpropedit.exe`, it will automatically download the latest compatible version of MKVToolNix and set it up for you, so you can start working right away without manual installation.

## 🏃 Running the Application

You can run the application easily using the provided launchers:

```bash
java -jar target/jmkvpropedit-1.5.2.jar
```

## 🛠️ Prerequisites
- **Java 8** or higher installed on your system.

---
*Developed with Vibe Coding*
