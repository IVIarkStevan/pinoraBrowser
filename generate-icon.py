#!/usr/bin/env python3
"""
Generate Pinora Browser icon from SVG to PNG and ICO formats
"""
import os
import sys

def create_png_icon():
    try:
        from PIL import Image, ImageDraw, ImageFont
        
        # Create a new image with gold background
        size = (256, 256)
        img = Image.new('RGBA', size, (26, 26, 26, 255))
        draw = ImageDraw.Draw(img)
        
        # Draw outer circle (dark gold border)
        border_color = (212, 175, 55, 255)  # Gold
        draw.ellipse([2, 2, 254, 254], outline=border_color, width=4)
        
        # Draw inner circle
        draw.ellipse([10, 10, 246, 246], outline=border_color, width=2)
        
        # Draw stars
        star_color = (255, 215, 0, 255)  # Gold
        
        def draw_star(x, y, size):
            points = []
            for i in range(10):
                angle = i * 36 - 90
                if i % 2 == 0:
                    r = size
                else:
                    r = size * 0.4
                import math
                px = x + r * math.cos(math.radians(angle))
                py = y + r * math.sin(math.radians(angle))
                points.append((px, py))
            draw.polygon(points, fill=star_color)
        
        # Top star
        draw.polygon([(128, 30), (133, 43), (147, 45), (137, 53), (141, 67), (128, 59), (115, 67), (119, 53), (109, 45), (123, 43)], fill=star_color)
        
        # Top left star
        draw.polygon([(70, 60), (74, 70), (85, 71), (77, 77), (80, 87), (70, 80), (60, 87), (63, 77), (55, 71), (66, 70)], fill=star_color)
        
        # Top right star
        draw.polygon([(186, 60), (190, 70), (201, 71), (193, 77), (196, 87), (186, 80), (176, 87), (179, 77), (171, 71), (182, 70)], fill=star_color)
        
        # Draw the "P" letter
        try:
            # Try to use a nice serif font
            font = ImageFont.truetype("/usr/share/fonts/truetype/liberation/LiberationSerif-Bold.ttf", 140)
        except:
            try:
                font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf", 140)
            except:
                font = ImageFont.load_default()
        
        draw.text((128, 140), "P", fill=(218, 165, 32, 255), font=font, anchor="mm")
        
        # Save as PNG
        png_path = '/home/stiler/Documents/code/devProject/pinoraBrowser/src/main/resources/icons/pinora.png'
        img.save(png_path, 'PNG')
        print(f"✓ Created PNG icon: {png_path}")
        
        # Resize and save as ICO
        ico_size = (128, 128)
        img_ico = img.resize(ico_size, Image.LANCZOS)
        ico_path = '/home/stiler/Documents/code/devProject/pinoraBrowser/src/main/resources/icons/pinora.ico'
        img_ico.save(ico_path, 'ICO')
        print(f"✓ Created ICO icon: {ico_path}")
        
        return True
    except ImportError:
        print("PIL not found, installing...")
        os.system("pip3 install pillow")
        return create_png_icon()
    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    if create_png_icon():
        print("\n✓ Icon generation successful!")
        sys.exit(0)
    else:
        print("\n✗ Icon generation failed")
        sys.exit(1)
