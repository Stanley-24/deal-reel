import pyttsx3
import os

def generate_narration(text: str, output_path: str) -> None:
    engine = pyttsx3.init()
    engine.save_to_file(text, output_path)
    engine.runAndWait()
