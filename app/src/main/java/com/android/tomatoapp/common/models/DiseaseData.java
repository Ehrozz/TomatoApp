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

        // TODO verify wording with advisers
        diseaseMap.put("Lady Beetle", new DiseaseInfo(
                "Many lady beetles are beneficial predators that eat aphids and other small pests. A few leaf-feeding species (Epilachna / 'peste ng talong') chew tomato leaves and can reduce yield if numbers are high.",
                "Adults and larvae of lady beetles; predatory species hunt aphids, while Epilachna beetles feed on foliage.",
                "• Watch plants weekly and identify the species before spraying\n" +
                        "• Conserve predatory lady beetles; avoid unnecessary insecticide\n" +
                        "• Hand-pick leaf-feeding beetles and larvae early in the morning\n" +
                        "• Keep weeds down around the plot",
                "Do not spray predatory lady beetles. For leaf-feeding Epilachna, hand-pick and destroy beetles and larvae, then use a labeled insecticide only if damage continues.",
                "Insect",
                "• Predatory species: round, spotted beetles hunting aphids on leaves\n" +
                        "• Leaf-feeding Epilachna: skeletonized leaves, holes, and yellowing\n" +
                        "• Larvae may be spiny and clustered on the underside of leaves",
                "Lady beetle → most are helpful aphid predators; Epilachna species chew tomato leaves and should be controlled by hand-picking first."
        ));

        diseaseMap.put("Whiteflies (Bemisia tabaci)", new DiseaseInfo(
                "Tiny white sap-sucking insects that cluster on the underside of tomato leaves. Heavy feeding weakens plants and they spread tomato leaf curl virus (TLCV/TYLCV), a major problem in Philippine tomato farms.",
                "Bemisia tabaci (silverleaf/whitefly). Populations explode in hot, dry weather and on weedy or crowded plantings.",
                "• Use yellow sticky traps and reflective mulch from seedling stage\n" +
                        "• Cover seedlings with insect-proof netting\n" +
                        "• Remove heavily infested and virus-infected plants\n" +
                        "• Avoid planting near infested tomato, eggplant, or tobacco",
                "Knock down adults with a strong water spray or insecticidal soap/neem. Use a labeled insecticide only if needed, covering leaf undersides, and rotate products to slow resistance.",
                "Insect",
                "• Clouds of tiny white insects when leaves are shaken\n" +
                        "• Sticky honeydew and sooty mold on leaves\n" +
                        "• Upward leaf curl, yellowing, and stunting if virus is present",
                "Bemisia tabaci → small white-winged fly on leaf undersides; a key vector of tomato leaf curl virus."
        ));

        diseaseMap.put("Aphids", new DiseaseInfo(
                "Soft-bodied sap suckers that colonize tender shoots, flower clusters, and the underside of young tomato leaves. They stunt growth and can spread plant viruses.",
                "Aphid colonies (often green or black) build up quickly in cool, lush growth and on weedy borders.",
                "• Check shoot tips and leaf undersides twice a week\n" +
                        "• Conserve lady beetles, hoverflies, and lacewings\n" +
                        "• Remove heavily infested shoots and nearby weeds\n" +
                        "• Avoid excess nitrogen that produces very soft growth",
                "Wash colonies off with water or spray insecticidal soap/neem on leaf undersides. Use a labeled insecticide only if colonies keep returning.",
                "Insect",
                "• Clusters of small green, yellow, or black insects on tender parts\n" +
                        "• Curled, sticky, or yellowing young leaves\n" +
                        "• Ants often attend the colonies for honeydew",
                "Aphids → soft-bodied sap feeders that cluster on young growth and may transmit viruses."
        ));

        diseaseMap.put("Flea Beetles", new DiseaseInfo(
                "Small jumping beetles that chew many tiny round holes in tomato leaves, especially on seedlings and young transplants. Severe feeding slows establishment in the field.",
                "Adult flea beetles hide in weeds and crop debris and jump when disturbed; they are most damaging in dry weather on young plants.",
                "• Use seedling trays or netting until plants are well established\n" +
                        "• Keep the plot and nearby weeds clean\n" +
                        "• Water and mulch to help plants outgrow early damage\n" +
                        "• Scout early in the morning when beetles are less active",
                "Protect seedlings with covers. If holes keep increasing, apply a labeled insecticide as a foliar spray, focusing on young plants.",
                "Insect",
                "• Numerous small round 'shot-hole' feeding marks on leaves\n" +
                        "• Tiny dark beetles that jump when the plant is touched\n" +
                        "• Seedlings may look ragged or delayed",
                "Flea beetle → small jumping leaf beetle; worst on seedlings and newly transplanted tomatoes."
        ));

        diseaseMap.put("Leaf Miner", new DiseaseInfo(
                "Tiny fly larvae tunnel inside tomato leaves, leaving winding white or silvery trails. Heavy mining reduces leaf area and can stress plants in the hot season.",
                "Larvae of leaf-mining flies (Liriomyza spp.) feed between the upper and lower leaf surfaces. Adults are small flies that lay eggs in leaves.",
                "• Remove and destroy mined leaves while larvae are still inside\n" +
                        "• Avoid spraying that kills beneficial parasites\n" +
                        "• Do not plant next to recently infested plots\n" +
                        "• Keep plants vigorous with balanced fertilizer and water",
                "Pick off badly mined leaves. If new mines keep appearing, use a labeled insecticide that reaches larvae inside the leaf, following the pre-harvest interval.",
                "Insect",
                "• Serpentine white or silvery trails on leaves\n" +
                        "• Mines later turn brown and leaves may dry\n" +
                        "• Small maggots inside the leaf if a mine is opened",
                "Leaf miner → fly larva that tunnels inside the leaf, leaving winding mines."
        ));

        diseaseMap.put("Melon Fly", new DiseaseInfo(
                "A fruit fly that stings developing tomato fruit and lays eggs inside. Maggots feed in the flesh, causing rot, premature drop, and unmarketable fruit—common in warm Philippine lowlands.",
                "Adult melon flies (Bactrocera cucurbitae and related fruit flies) puncture fruit to lay eggs; larvae develop inside.",
                "• Bag young fruits with paper or fine mesh where practical\n" +
                        "• Collect and bury or burn fallen and infested fruit daily\n" +
                        "• Use protein bait traps around the field\n" +
                        "• Harvest promptly; do not leave ripe fruit on the plant",
                "There is no cure once maggots are inside the fruit—remove and destroy infested fruit. Protect remaining fruit with bagging and bait traps; use a labeled bait spray only as directed.",
                "Insect",
                "• Tiny puncture marks or soft spots on fruit\n" +
                        "• Fruit that yellows, rots, or drops early\n" +
                        "• White maggots in the flesh when fruit is cut",
                "Melon fly → fruit fly that oviposits in tomato fruit; larvae feed inside and spoil the harvest."
        ));

        diseaseMap.put("Worms (Caterpillars)", new DiseaseInfo(
                "Moth larvae (fruitworms, armyworms, hornworms, and leaf-feeding caterpillars) chew tomato leaves, flowers, and fruit. Fruit borers hide inside the fruit and cause rotting.",
                "Eggs are laid on leaves or near flowers; larvae feed at night or inside fruit. Outbreaks often follow flowering and fruit set.",
                "• Scout leaves, flowers, and fruit at least twice a week\n" +
                        "• Hand-pick larvae and egg masses in the morning\n" +
                        "• Remove damaged fruit so larvae cannot finish development\n" +
                        "• Encourage birds and beneficial wasps; avoid blanket spraying",
                "Hand-pick visible worms. For fruit borers or heavy leaf feeding, apply Bacillus thuringiensis (Bt) or a labeled insecticide, covering flowers and fruit, and observe the pre-harvest interval.",
                "Insect",
                "• Chewed leaves, missing growing tips, or holes in fruit\n" +
                        "• Dark droppings (frass) on leaves or near fruit openings\n" +
                        "• Caterpillars on foliage or hidden inside fruit",
                "Caterpillar → moth larva that chews leaves or bores into tomato fruit; look for frass and entry holes."
        ));
    }

    public static String findFullName(String name) {
        if (name == null) return null;
        String n = name.trim().toLowerCase();
        for (String key : diseaseMap.keySet()) {
            String k = key.toLowerCase();
            if (k.equals(n) || k.startsWith(n + " (")) return key;
        }
        return null;
    }

    public static DiseaseInfo getDiseaseInfo(String diseaseName) {
        return diseaseMap.get(diseaseName);
    }
}
