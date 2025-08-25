package com.android.tomatoapp;

import java.util.HashMap;

public class DiseaseData {

    private static final HashMap<String, DiseaseInfo> diseaseMap = new HashMap<>();

    static {

        // ✅ Healthy Tomato (no disease)
        diseaseMap.put("Healthy Tomato", new DiseaseInfo(
                "This tomato plant shows no visible signs of disease. Leaves, stems, and fruits appear normal and healthy.",
                "No disease-causing pathogen present.",
                "• Maintain good farming practices\n" +
                        "• Ensure proper irrigation, spacing, and nutrition\n" +
                        "• Regularly monitor for early signs of pests or diseases",
                "No treatment needed. Continue with preventive care and monitoring.",
                "None",
                "No symptoms present. Plant is vigorous, leaves are green, stems strong, and fruits developing normally.",
                "No pests present."
        ));

        // 🔹 Tomato Leaf Curl Virus (TLCV)
        diseaseMap.put("Tomato Leaf Curl Virus (TLCV)", new DiseaseInfo(
                "TYLCV-infected tomato plants show stunted growth, yellowing and curling of leaves, reduced flowering, and poor fruit set. Severe cases can result in 50–100% yield loss.",
                "Viral disease transmitted mainly by the insect vector whitefly (Bemisia tabaci). Can also be possibly seed-transmitted.",
                "• Control whitefly using insect-proof nets, sticky traps, and reflective mulches\n" +
                        "• Uproot and destroy infected plants\n" +
                        "• Practice crop rotation with non-host crops\n" +
                        "• Use resistant/tolerant varieties\n" +
                        "• Apply neem-based or biocontrol agents against vectors",
                "- Use resistant/tolerant varieties (PCAARRD and UPLB are developing new lines).\n" +
                        "- Control whiteflies with yellow sticky traps, reflective mulches, neem-based sprays, or safe insecticides.\n" +
                        "- Practice crop rotation and avoid planting tomatoes near infected crops.",
                "Virus (spread by whitefly)",
                "• Leaf curling (young leaves curl upward/inward)\n" +
                        "• Leaf distortion (crinkling, puckering, leathery texture)\n" +
                        "• Yellowing around veins\n" +
                        "• Stunted growth, reduced branching\n" +
                        "• Premature flower drop, poor fruit set\n" +
                        "• Yield loss up to 100%",
                "Whitefly (Bemisia tabaci) → Small sap-sucking insect with powdery white wings. Feeds on leaf undersides, causing leaf curling, yellowing, and transmitting TLCV."
        ));

        // 🔹 Early Blight (Alternaria solani)
        diseaseMap.put("Early Blight (Alternaria solani)", new DiseaseInfo(
                "A fungal disease of tomato and potato caused by Alternaria solani. Infection usually starts on older leaves near the ground and can affect leaves, stems, and fruits.",
                "Soilborne fungus that thrives in warm, humid, wet conditions. Spreads by wind, rain splash, contaminated tools, and crop debris.",
                "• Rotate crops for 2–3 years\n" +
                        "• Remove infected debris\n" +
                        "• Avoid overhead irrigation\n" +
                        "• Use resistant varieties\n" +
                        "• Apply mulch to reduce soil splash\n" +
                        "• Fungicide sprays (chlorothalonil, mancozeb) at first symptoms",
                "- Use resistant varieties and healthy seedlings.\n" +
                        "- Spray with fungicides (mancozeb, chlorothalonil, copper-based fungicides).\n" +
                        "- Remove and burn infected leaves.\n" +
                        "- Ensure wide spacing and pruning for airflow.",
                "Fungus",
                "Leaves: Small dark spots with concentric rings → yellowing → premature drop.\n" +
                        "Stems: Dark, sunken lesions, can cause collar rot.\n" +
                        "Fruits: Sunken dark spots near stem end with concentric rings.",
                "Alternaria solani → soilborne fungus surviving in debris, spreads by rain splash, wind, and contaminated tools."
        ));

        // 🔹 Late Blight (Phytophthora infestans)
        diseaseMap.put("Late Blight (Phytophthora infestans)", new DiseaseInfo(
                "Highly destructive disease that can devastate entire tomato fields within days under favorable conditions.",
                "Causal organism: Phytophthora infestans, a water mold (oomycete). Survives in plant debris and spreads rapidly via spores in cool, wet conditions.",
                "• Plant resistant varieties\n" +
                        "• Use well-drained fields\n" +
                        "• Apply copper-based fungicides preventively\n" +
                        "• Monitor fields and destroy infected plants\n" +
                        "• Rotate crops, avoid potatoes nearby",
                "- Apply fungicides (metalaxyl, chlorothalonil, mancozeb).\n" +
                        "- Use mulch to avoid water splash.\n" +
                        "- Stake plants for better airflow.\n" +
                        "- Destroy crop debris after harvest.",
                "Oomycete (fungus-like)",
                "Leaves: Large, irregular water-soaked lesions; white mold underside.\n" +
                        "Stems: Dark brown-black lesions, may girdle stems.\n" +
                        "Fruits: Water-soaked brown lesions, white mold under wet conditions.",
                "Phytophthora infestans → spreads via wind and rain, thrives in cool humid weather, infects tomato & potato."
        ));

        // 🔹 Bacterial Wilt (Ralstonia solanacearum)
        diseaseMap.put("Bacterial Wilt (Ralstonia solanacearum)", new DiseaseInfo(
                "Causes sudden wilting of tomato plants, even when soil moisture is adequate.",
                "Soilborne bacterium entering through root wounds. Persists in soil, infects vascular tissue, blocks water movement.",
                "• Rotate with cereals/legumes\n" +
                        "• Soil solarization\n" +
                        "• Use resistant or grafted rootstocks\n" +
                        "• Improve drainage; avoid waterlogging\n" +
                        "• Sanitize tools; remove infected plants",
                "- Use resistant varieties (identified by PCAARRD & UPLB).\n" +
                        "- Rotate with non-solanaceous crops.\n" +
                        "- Solarize soil with clear plastic.\n" +
                        "- Improve drainage.",
                "Bacterium (soil-borne)",
                "• Sudden wilting without yellowing\n" +
                        "• Leaves remain green while wilting\n" +
                        "• Brown vascular streaks inside stems\n" +
                        "• Sticky bacterial ooze in cut stems",
                "Ralstonia solanacearum → soilborne bacterium that spreads via infected soil, water, or plant debris. Difficult to eradicate."
        ));

        // 🔹 Fusarium Wilt (Fusarium oxysporum)
        diseaseMap.put("Fusarium Wilt (Fusarium oxysporum)", new DiseaseInfo(
                "Soilborne fungus that infects roots, spreads through vascular tissue, and causes wilting.",
                "Persists in soil for many years. Enters through root wounds and blocks water transport.",
                "• Plant resistant/tolerant varieties\n" +
                        "• Rotate with non-host crops\n" +
                        "• Soil sterilization (solarization/steam)\n" +
                        "• Apply organic amendments, Trichoderma biocontrol\n" +
                        "• Avoid root injury; maintain balanced fertilization",
                "- Use biocontrol products like WiltCure.\n" +
                        "- Plant resistant varieties.\n" +
                        "- Rotate crops.\n" +
                        "- Use Trichoderma-based soil treatments.",
                "Fungus",
                "• Lower leaves turn yellow → progresses upward\n" +
                        "• Wilting during day but recovery at night (early)\n" +
                        "• Brown vascular streaks inside stems\n" +
                        "• Stunted growth, eventual plant death",
                "Fusarium oxysporum → soilborne fungus spreading via debris, contaminated soil, and water."
        ));

        // 🔹 Anthracnose (Colletotrichum spp.)
        diseaseMap.put("Anthracnose (Colletotrichum spp.)", new DiseaseInfo(
                "Fungal disease that mainly affects ripening fruits, causing sunken black spots.",
                "Caused by Colletotrichum spp. Spores spread by rain splash, irrigation, insects, and contaminated tools.",
                "• Sanitation: remove infected fruits/debris\n" +
                        "• Rotate crops\n" +
                        "• Mulching and irrigation management\n" +
                        "• Fungicide sprays before harvest\n" +
                        "• Proper post-harvest handling",
                "- Harvest fruits on time.\n" +
                        "- Use fungicides (copper-based, mancozeb).\n" +
                        "- Hot-water seed treatment.\n" +
                        "- Destroy infected fruits.",
                "Fungus",
                "Fruits: Small, sunken dark lesions with salmon-colored spore masses.\n" +
                        "Leaves: Irregular necrotic spots, yellowing.\n" +
                        "Stems: Elongated dark lesions.",
                "Colletotrichum spp. → fungus infecting fruits, survives in crop debris, spread by splashing water."
        ));

        // 🔹 Black Leaf Mold (Pseudocercospora fuligena)
        diseaseMap.put("Black Leaf Mold (Pseudocercospora fuligena)", new DiseaseInfo(
                "Fungal disease causing yellow patches on leaves and black mold underneath.",
                "Fungus infects older leaves under humid conditions. Spreads via wind-blown spores.",
                "• Apply NVSU FD-BIOPEST mycopesticide weekly from 3–14 weeks after transplanting.\n" +
                        "• Improve airflow\n" +
                        "• Use resistant lines when available",
                "- Improve ventilation (avoid humidity).\n" +
                        "- Spray fungicides (mancozeb, copper, chlorothalonil).\n" +
                        "- Use organic biopesticides like NVSU FD-BIOPEST.",
                "Fungus",
                "Leaves: Yellow patches, underside with black mold growth.\n" +
                        "Advanced: Premature leaf drop, defoliation, reduced yield.\n" +
                        "Rarely: Dark lesions on stems/calyx.",
                "Pseudocercospora fuligena → fungal pathogen surviving in debris, spreads by wind and rain in humid conditions."
        ));
    }

    public static DiseaseInfo getDiseaseInfo(String diseaseName) {
        return diseaseMap.get(diseaseName);
    }
}
