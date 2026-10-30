package com.example.data.model

data class PromptCategory(
    val id: String,
    val name: String,
    val iconEmoji: String
)

object PromptCatalog {
    val categories = listOf(
        PromptCategory("all", "All Prompts", "🌐"),
        PromptCategory("transparent_png", "Transparent PNGs", "💎"),
        PromptCategory("3d_typography", "3D Typography", "✨"),
        PromptCategory("gold_metallic", "Gold & Metallic", "👑"),
        PromptCategory("neon_glow", "Neon & Glowing", "⚡"),
        PromptCategory("logo_creation", "Logo Creation", "🎯"),
        PromptCategory("funny_prompts", "Funny AI", "😂"),
        PromptCategory("cinematic_photo", "Cinematic Photo", "🎬"),
        PromptCategory("realistic_portraits", "Realistic Portraits", "👤"),
        PromptCategory("fantasy_art", "Fantasy & Sci-Fi", "🧙‍♂️"),
        PromptCategory("social_media", "Social Media Graphics", "📱"),
        PromptCategory("youtube_thumbnails", "YouTube Thumbnails", "▶️"),
        PromptCategory("posters_banners", "Posters & Banners", "🖼️"),
        PromptCategory("cartoon_illustration", "Cartoon & Anime", "🎨"),
        PromptCategory("branding", "Branding & Identity", "💼"),
        PromptCategory("architecture", "Architecture & Interior", "🏛️"),
        PromptCategory("product_photo", "Product Photography", "📦"),
        PromptCategory("video_gen", "Video Generation", "🎥"),
        PromptCategory("business_marketing", "Business & Marketing", "📈"),
        PromptCategory("creative_experiments", "Creative Experiments", "🧪")
    )

    val curatedPrompts = listOf(
        WorldwidePrompt(
            id = "p_trans_1",
            title = "Floating Holographic Cyber Sphere",
            prompt = "A high-tech floating holographic sphere with interlocking neon circuit pathways, cyan and gold glowing runes, isolated on a completely transparent background, PNG asset, crisp alpha transparency, 8k resolution.",
            category = "Transparent PNGs",
            description = "Creates an asset-ready transparent sci-fi icon for UI and gaming.",
            aiTool = "Gemini / Midjourney",
            trending = true,
            tags = listOf("transparent", "hologram", "ui", "cyberpunk")
        ),
        WorldwidePrompt(
            id = "p_gold_1",
            title = "3D Baroque Molten Gold Lettering",
            prompt = "Ultra-luxurious 3D typography spelling 'INFINITY' sculpted from molten liquid gold with filigree baroque ornaments, radiant caustics, warm specular highlights, cinematic studio lighting, isolated transparent PNG cutout.",
            category = "Gold & Metallic",
            description = "High-end luxury metallic lettering with exquisite lighting.",
            aiTool = "Gemini / Stable Diffusion",
            trending = true,
            tags = listOf("3d", "gold", "typography", "luxury")
        ),
        WorldwidePrompt(
            id = "p_neon_1",
            title = "Electric Blue Cyberpunk Samurai Crest",
            prompt = "A minimalist cyberpunk samurai helmet emblem, glowing electric blue and violet neon tubing, retro-futuristic synthwave aesthetic, isolated on transparent background, clean vector lines.",
            category = "Neon & Glowing",
            description = "Iconic neon emblem with deep electric blue glow.",
            aiTool = "Gemini",
            trending = true,
            tags = listOf("neon", "cyberpunk", "blue", "sticker")
        ),
        WorldwidePrompt(
            id = "p_logo_1",
            title = "Futuristic AI Studio Geometric Logo",
            prompt = "Minimalist modern tech logo of an abstract folded origami phoenix infused with neural network nodes, deep midnight navy and gold metallic gradient, vector flat art, transparent background.",
            category = "Logo Creation",
            description = "Award-winning corporate identity concept for creative AI startups.",
            aiTool = "Gemini / Illustrator",
            trending = true,
            tags = listOf("logo", "branding", "minimalist", "tech")
        ),
        WorldwidePrompt(
            id = "p_funny_1",
            title = "Astronaut Cat Ordering Pizza in Deep Space",
            prompt = "A hyper-expressive ginger tabby cat wearing a high-tech NASA astronaut helmet, floating outside an orbital space pizza parlor trying to decide on pepperoni toppings, humorous Pixar animation style, warm volumetric lighting.",
            category = "Funny AI",
            description = "Hilarious and whimsical character scene guaranteed to entertain.",
            aiTool = "Gemini / DALL-E",
            trending = true,
            tags = listOf("funny", "cat", "space", "animation")
        ),
        WorldwidePrompt(
            id = "p_cine_1",
            title = "Midnight Cyber Rain in Neo-Tokyo",
            prompt = "Cinematic 35mm film shot of an enigmatic traveler standing under a transparent umbrella in a neon-drenched Tokyo alleyway at night, reflections on wet asphalt, anamorphic blue lens flare, shallow depth of field, Kodachrome color grade.",
            category = "Cinematic Photo",
            description = "Atmospheric film-still aesthetic with intense cinematic mood.",
            aiTool = "Midjourney / Gemini",
            trending = false,
            tags = listOf("cinematic", "film", "tokyo", "rain")
        ),
        WorldwidePrompt(
            id = "p_portrait_1",
            title = "Cybernetic Empress with Golden Accents",
            prompt = "High-fashion editorial portrait of a woman with delicate porcelain skin and subtle golden cybernetic filigree along her cheekbones, luminous crystal blue eyes, dramatic studio chiaroscuro lighting, Vogue magazine aesthetic.",
            category = "Realistic Portraits",
            description = "Striking editorial portrait blending beauty with cybernetic elegance.",
            aiTool = "Gemini",
            trending = true,
            tags = listOf("portrait", "cyberpunk", "gold", "fashion")
        ),
        WorldwidePrompt(
            id = "p_thumb_1",
            title = "High-CTR Epic Tech Reveal Thumbnail",
            prompt = "YouTube thumbnail visual: Split composition featuring an ultra-futuristic glowing holographic gadget bursting with electric sparks on the left, dramatic lighting, high contrast, vivid electric blue and neon yellow colors, optimized for click-through rate.",
            category = "YouTube Thumbnails",
            description = "Engineered to maximize engagement on video platforms.",
            aiTool = "Gemini / Photoshop",
            trending = true,
            tags = listOf("youtube", "thumbnail", "marketing", "tech")
        ),
        WorldwidePrompt(
            id = "p_social_1",
            title = "Glassmorphic Product Launch Instagram Story",
            prompt = "Vertical 9:16 layout showcasing a luxury smart device on a floating frosted glass podium, surrounded by subtle golden particles and soft electric blue light streaks, minimalist modern typography layout, clean aesthetic.",
            category = "Social Media Graphics",
            description = "Premium vertical story layout for luxury brands.",
            aiTool = "Gemini / Canva",
            trending = false,
            tags = listOf("instagram", "story", "luxury", "social")
        ),
        WorldwidePrompt(
            id = "p_3d_1",
            title = "Inflated Glossy Chrome Balloon Lettering",
            prompt = "Playful 3D inflated balloon typography spelling 'DISCOVER', mirror-finish metallic chrome reflecting vibrant neon city lights, soft ambient occlusion, hyper-realistic 3D octane render.",
            category = "3D Typography",
            description = "Trendy 3D balloon chrome lettering with glossy reflections.",
            aiTool = "Gemini / Blender",
            trending = true,
            tags = listOf("3d", "chrome", "letters", "balloon")
        ),
        WorldwidePrompt(
            id = "p_arch_1",
            title = "Biophilic Luxury Penthouse in the Clouds",
            prompt = "Interior architectural photography of an ultra-modern cliffside penthouse above the clouds, floor-to-ceiling glass curved walls, integrated indoor waterfalls and lush exotic flora, minimalist dark oak and brass furniture, golden hour sunlight.",
            category = "Architecture & Interior",
            description = "Dreamlike architectural rendering with serene natural harmony.",
            aiTool = "Gemini",
            trending = false,
            tags = listOf("architecture", "interior", "luxury", "modern")
        ),
        WorldwidePrompt(
            id = "p_sticker_1",
            title = "Holographic Gaming Pixel Sword Sticker",
            prompt = "Die-cut sticker of an enchanted cyber sword with crackling lightning, thick white border, iridescent holographic foil reflection, clean die-cut transparent edges, vector art.",
            category = "Transparent PNGs",
            description = "Ready-to-print gaming sticker asset with genuine transparency.",
            aiTool = "Gemini",
            trending = true,
            tags = listOf("sticker", "gaming", "transparent", "diecut")
        )
    )
}
