# src/video_generator/script_generator.py

# Legacy sample/mock and HTML generation kept for reference
# (Not used by the Veo backend flow)

def fetch_product_by_asin(asin: str) -> dict:
    # Replace this with your real DB fetch logic
    if asin == "B07PGL2ZSL":
        return {
            "title": "CozyMom Baby Carrier",
            "price": "$39.99",
            "description": "carrying your baby comfortably all day",
            "asin": asin
        }
    raise ValueError(f"Product with ASIN {asin} not found.")

STORY_TEMPLATE_HTML = """
<p><strong>Mother:</strong> "Sweetheart, today I have something special to show you."</p>
<p><strong>Daughter:</strong> "What is it, Mom? It looks so pretty!"</p>
<p><strong>Mother:</strong> "It's the <em>{title}</em>, a wonderful product that has made our lives easier and happier."</p>
<p><strong>Daughter:</strong> "Wow! And it costs only {price}? That's amazing!"</p>
<p><strong>Mother:</strong> "Yes, and it's perfect for {description}. Just imagine all the fun and memories we'll create together."</p>
<p><strong>Daughter:</strong> "I love it, Mom! Thank you for finding the <em>{title}</em>."</p>
<p><strong>Mother:</strong> "You're welcome, darling. Sharing moments like this is what makes life beautiful."</p>
<p><strong>Mother & Daughter together:</strong> "Get your own <em>{title}</em> today and start creating memories together!"</p>
<p><em>Buy now at an unbeatable price of {price}. Visit our store and use ASIN {asin} to find it quickly!</em></p>
"""

def generate_story_script_html(asin: str) -> str:
    product = fetch_product_by_asin(asin)
    return STORY_TEMPLATE_HTML.format(
        title=product["title"],
        price=product["price"],
        description=product["description"],
        asin=product["asin"]
    )

# New: Build a Veo prompt using only product title, description, and price
# Focus: 6-second vertical video, chipmunk character promoting product with CTA

def build_chipmunk_veo_prompt(title: str, description: str, price: str) -> str:
    lines = [
        "Create a short 6-second vertical product ad video.",
        "Primary character: an energetic, cute chipmunk as the presenter.",
        f"Product name: {title}.",
        f"Product description: {description}.",
        f"Price: {price}.",
        "Tone: playful, upbeat, crisp visuals, dynamic camera, studio-quality lighting.",
        "Storytelling arc (very brief): 1) quick hook by chipmunk presenter, 2) one standout benefit from the description, 3) price flash, 4) compelling CTA.",
        "CTA text: 'Buy now' or 'Shop today' (keep it short).",
        "Visual style: clean product showcase with chipmunk interacting or gesturing towards the product.",
        "Output: 1080x1920 vertical, high clarity, modern typography for price and CTA.",
    ]
    return "\n".join(lines)
