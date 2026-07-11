from PIL import Image

# Open the image
img = Image.open('app/src/main/res/drawable/icon_image_1780221523424.png')

# Resize to 432x432 (108dp at xxxhdpi)
img_resized = img.resize((432, 432), Image.Resampling.LANCZOS)

# Save to the new location
import os
os.makedirs('app/src/main/res/drawable-xxxhdpi', exist_ok=True)
img_resized.save('app/src/main/res/drawable-xxxhdpi/icon_image_1780221523424.png')

print("Image resized and saved to drawable-xxxhdpi")
