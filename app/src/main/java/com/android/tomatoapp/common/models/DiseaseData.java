package com.android.tomatoapp.common.models;

import java.util.HashMap;

public class DiseaseData {

    private static final HashMap<String, DiseaseInfo> diseaseMap = new HashMap<>();

    static {

        diseaseMap.put("Healthy Tomato", new DiseaseInfo(
                "This tomato fruit shows no visible signs of disease and may be ripe or unripe. Harvest timing depends on the fruit's ripeness.",
                "No disease-causing pathogen present.",
                "• Maintain good farming practices\n" +
                        "• Ensure proper irrigation, spacing, and nutrition\n" +
                        "• Regularly monitor for early signs of pests or diseases",
                "No treatment needed. Continue with preventive care and monitoring.",
                "None",
                "No symptoms present. Plant is vigorous, leaves are green, stems strong, and fruits developing normally.",
                "No pests present."
        ));

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
        diseaseMap.put("Tomato Yellow Leaf Curl Virus (TYLCV)",
                diseaseMap.get("Tomato Leaf Curl Virus (TLCV)"));

        diseaseMap.put("Bacterial Spot (Xanthomonas spp.)", new DiseaseInfo(
                "A bacterial disease causing dark leaf spots and lesions on tomato fruit.",
                "Xanthomonas bacteria spread by infected seed, rain splash, tools, and wet foliage.",
                "• Use certified disease-free seed and resistant varieties\n" +
                        "• Avoid overhead irrigation and work in wet crops\n" +
                        "• Remove infected debris and rotate crops\n" +
                        "• Disinfect tools and provide good plant spacing",
                "Remove severely infected leaves. Apply copper-based bactericides according to the label; rotate products and observe the pre-harvest interval.",
                "Bacteria",
                "Small water-soaked spots that turn dark brown, yellow halos, leaf drop, and raised or scabby fruit spots.",
                "Xanthomonas spp. → bacteria favored by warm, wet weather and spread by splashing water."
        ));

        diseaseMap.put("Leaf Mold (Passalora fulva)", new DiseaseInfo(
                "A fungal disease that mainly affects tomato leaves in humid, poorly ventilated conditions.",
                "Passalora fulva spores spread through air, water splash, infected debris, and contaminated seed.",
                "• Improve airflow and reduce humidity\n" +
                        "• Space and prune plants properly\n" +
                        "• Avoid wetting leaves and remove infected debris\n" +
                        "• Plant resistant varieties",
                "Remove affected leaves and apply a registered fungicide such as chlorothalonil, mancozeb, or copper according to the label.",
                "Fungus",
                "Pale yellow spots on the upper leaf surface with olive-green to brown mold on the underside, followed by leaf death.",
                "Passalora fulva → fungus favored by high humidity and persistent leaf wetness."
        ));

        diseaseMap.put("Spider Mites (Tetranychus urticae)", new DiseaseInfo(
                "Tiny sap-feeding pests that can weaken tomato plants and cause bronzing or leaf loss.",
                "Tetranychus urticae populations increase in hot, dry weather and spread on plants, tools, and clothing.",
                "• Monitor leaf undersides regularly\n" +
                        "• Reduce dust and maintain adequate irrigation\n" +
                        "• Conserve predatory mites and other beneficial insects\n" +
                        "• Remove heavily infested leaves",
                "Use a labeled miticide or insecticidal soap/neem product with thorough underside coverage; repeat only as directed and rotate modes of action.",
                "Mite",
                "Fine stippling, yellowing or bronzing, webbing under leaves, leaf curling, and premature leaf drop.",
                "Tetranychus urticae → very small mites found mostly on leaf undersides, favored by heat and dry conditions."
        ));

        diseaseMap.put("Tomato Mosaic Virus (ToMV)", new DiseaseInfo(
                "A persistent viral disease that can reduce tomato growth, leaf quality, and fruit yield.",
                "Tomato mosaic virus spreads through infected seed, plant sap, hands, tools, and plant contact.",
                "• Use certified seed and resistant varieties\n" +
                        "• Wash hands and disinfect tools frequently\n" +
                        "• Remove infected plants and crop debris\n" +
                        "• Avoid handling plants while tobacco products are present",
                "There is no cure. Remove and destroy infected plants; do not compost them, and control spread through strict sanitation.",
                "Virus",
                "Mottled light and dark green leaves, leaf distortion, stunting, uneven fruit ripening, and reduced fruit quality.",
                "Tomato mosaic virus → stable virus spread mechanically through sap, seed, tools, and handling."
        ));

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

        diseaseMap.put("Blossom End Rot", new DiseaseInfo(
                "A non-infectious physiological disorder causing a dark, sunken lesion at the blossom end of the fruit.",
                "Inconsistent water supply and poor calcium movement to developing fruit, often worsened by root stress.",
                "• Keep soil moisture even with mulch and regular irrigation\n" +
                        "• Avoid root damage and excessive nitrogen\n" +
                        "• Maintain balanced soil fertility",
                "Remove affected fruit and maintain steady watering; follow the product label for any soil amendment.",
                "None",
                "Brown or black, firm, sunken lesion on the blossom end that may enlarge as the fruit develops.",
                "No pest causes this physiological disorder."
        ));

        diseaseMap.put("Blotchy Ripening", new DiseaseInfo(
                "A non-infectious physiological disorder causing uneven color development and gray or yellow patches on ripe fruit.",
                "Uneven ripening related to temperature, nutrition, light, and plant stress; some cases are associated with whitefly feeding.",
                "• Maintain balanced nutrition and consistent irrigation\n" +
                        "• Improve canopy health and airflow\n" +
                        "• Use tolerant varieties where available",
                "There is no direct cure; remove severely affected fruit and follow the product label for any permitted pest control.",
                "None",
                "Fruit remains green, yellow, or gray in irregular patches while other areas turn red; internal tissue may remain pale.",
                "Usually physiological; inspect plants for whiteflies if feeding damage is suspected."
        ));

        diseaseMap.put("Fruit Cracking", new DiseaseInfo(
                "A non-infectious physiological disorder in which tomato skin splits as fruit expands.",
                "Rapid fruit expansion after irregular watering, high humidity, or sudden changes in moisture.",
                "• Keep soil moisture consistent\n" +
                        "• Mulch and avoid severe pruning\n" +
                        "• Harvest promptly when fruit reaches maturity",
                "Harvest cracked fruit promptly and remove badly damaged fruit; follow the product label for any protective treatment.",
                "None",
                "Radial cracks from the stem end or concentric cracks around the shoulder, often exposing inner tissue.",
                "No pest causes this disorder, though exposed fruit can attract secondary pests."
        ));

        diseaseMap.put("Sun Scald", new DiseaseInfo(
                "A non-infectious physiological disorder caused by excessive direct sunlight on exposed tomato fruit.",
                "Fruit is exposed after defoliation, pruning, disease, or weak canopy cover during hot bright weather.",
                "• Maintain healthy foliage and avoid excessive pruning\n" +
                        "• Provide shade during extreme heat\n" +
                        "• Maintain adequate soil moisture",
                "Remove severely damaged fruit and protect developing fruit from intense sun; follow the product label for any permitted protective treatment.",
                "None",
                "Pale yellow or white, firm patch on the sun-facing side that may become papery or necrotic.",
                "No pest causes this physiological disorder."
        ));

        diseaseMap.put("Fruit Worm (Fruit Borer)", new DiseaseInfo(
                "Fruit-feeding caterpillars bore into tomato fruit and damage the flesh and seeds.",
                "Larvae of fruit-boring moths, commonly Helicoverpa species, feed on flowers and developing fruit.",
                "• Scout flowers and fruit regularly\n" +
                        "• Remove damaged fruit and crop debris\n" +
                        "• Use nets or biological controls where practical",
                "Hand-pick larvae where possible and use an approved insecticide or biological control; follow the product label.",
                "Caterpillar",
                "Small entry holes, frass near the opening, internal feeding tunnels, and rotting fruit.",
                "Fruit-boring caterpillar → usually hides inside fruit during feeding and may leave dark droppings near entry holes."
        ));

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
