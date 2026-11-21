package com.android.tomatoapp.common.models;

import java.util.HashMap;

public class DiseaseData {

    private static final HashMap<String, DiseaseInfo> diseaseMap = new HashMap<>();

    public static final String TREATMENT_DISCLAIMER =
        "This is general guidance, not a substitute for professional advice. Results vary with "
      + "weather, pathogen strain and local pesticide resistance. Consult your municipal "
      + "agriculturist, the DA/BPI office or a licensed agronomist before treating, especially "
      + "before applying any pesticide. Use only FPA-registered products, follow the label "
      + "(dose, pre-harvest interval, protective gear), and rotate products.";

    // Constructor order: description, cause, prevention, cure, type, symptoms, pestDescription
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
                "TYLCV-infected tomato plants show stunted growth, yellowing and upward curling of young leaves, reduced flowering, and poor fruit set. Severe infections can cause major yield loss.",
                "Viral disease transmitted primarily by whiteflies (Bemisia tabaci). Use certified, virus-tested seed and transplants; seed transmission is not the main route of field spread.",
                "• Control whiteflies from transplanting onward\n" +
                        "• Use insect-proof netting, reflective mulch, and yellow sticky traps\n" +
                        "• Remove and destroy virus-infected plants early\n" +
                        "• Manage weeds and volunteer tomato plants\n" +
                        "• Use resistant or tolerant varieties where available\n" +
                        "• Use only FPA-registered insecticides when whitefly control is needed",
                "There is no cure for an infected plant. Remove and destroy infected plants early to reduce the virus source, and manage whiteflies to limit further spread. Plant resistant or tolerant varieties in the next crop.",
                "Virus (spread by whitefly)",
                "• Young leaves curl upward or inward\n" +
                        "• Leaf distortion, crinkling, or leathery texture\n" +
                        "• Yellowing, especially along veins\n" +
                        "• Stunted growth and reduced branching\n" +
                        "• Flower drop and poor fruit set\n" +
                        "• Reduced yield in severe cases",
                "Whitefly (Bemisia tabaci) → Small sap-sucking insect with powdery white wings. It feeds on leaf undersides and is the main vector of tomato yellow leaf curl virus."
        ).withNoTreatment());

        diseaseMap.put("Tomato Yellow Leaf Curl Virus (TYLCV)",
                diseaseMap.get("Tomato Leaf Curl Virus (TLCV)"));

        diseaseMap.put("Bacterial Spot (Xanthomonas spp.)", new DiseaseInfo(
                "A bacterial disease causing dark leaf spots, leaf yellowing and drop, and lesions or scabby spots on tomato fruit.",
                "Xanthomonas bacteria spread through infected seed or transplants, rain splash, contaminated tools, hands, and wet foliage.",
                "• Use certified disease-free seed and transplants\n" +
                        "• Avoid overhead irrigation and avoid working in wet foliage\n" +
                        "• Remove infected debris and practice crop rotation\n" +
                        "• Disinfect tools and maintain good plant spacing\n" +
                        "• Use resistant varieties where available",
                "No product cures existing bacterial-spot lesions. Remove severely infected leaves only when foliage is dry. Copper-based bactericides may suppress further spread when applied early and strictly according to the label, but they will not eliminate existing lesions. Copper-resistant bacterial strains can occur, so prevention and sanitation are essential.",
                "Bacteria",
                "Small water-soaked spots that become dark brown or black, often with yellow halos; leaf yellowing and drop; raised, scabby, or cracked fruit lesions.",
                "Xanthomonas spp. → Bacteria favored by warm, wet conditions and spread by splashing water, contaminated tools, and infected planting material."
        ));

        diseaseMap.put("Leaf Mold (Passalora fulva)", new DiseaseInfo(
                "A fungal disease that mainly affects tomato leaves under humid, poorly ventilated conditions, especially in protected or densely planted crops.",
                "Passalora fulva spores spread through air currents, water splash, infected leaf debris, and contaminated seed or transplants.",
                "• Improve airflow and reduce humidity\n" +
                        "• Space and prune plants appropriately\n" +
                        "• Avoid wetting foliage during irrigation\n" +
                        "• Remove infected leaves and crop debris\n" +
                        "• Use resistant varieties where available",
                "Remove heavily affected leaves and improve ventilation to reduce humidity. Use only a fungicide whose current Philippine label lists tomato and leaf mold; chlorothalonil or mancozeb may be options only if specifically labeled for that use. Fungicides protect healthy tissue but do not restore already infected leaves. Resistant varieties and humidity management are the most reliable long-term controls.",
                "Fungus",
                "Pale yellow spots on the upper leaf surface with olive-green to brown fungal growth on the underside; severely affected leaves may yellow, dry, and drop.",
                "Passalora fulva → Fungus favored by high humidity, persistent leaf wetness, and poor airflow."
        ));
                diseaseMap.put("Spider Mites (Tetranychus urticae)", new DiseaseInfo(
                "Tiny sap-feeding pests that can weaken tomato plants and cause bronzing or leaf loss.",
                "Tetranychus urticae populations increase in hot, dry weather and spread on plants, tools, and clothing.",
                "• Monitor leaf undersides regularly\n" +
                        "• Reduce dust and maintain adequate irrigation\n" +
                        "• Conserve predatory mites and other beneficial insects\n" +
                        "• Remove heavily infested leaves",
                "Spray the leaf undersides with a strong stream of water to knock mites down. Insecticidal soap "
                        + "or neem/horticultural oil works with thorough underside coverage, repeated as the label "
                        + "directs. For heavy infestations use a miticide registered for tomato and rotate modes of "
                        + "action. Avoid broad-spectrum insecticides, which kill predatory mites and can make outbreaks worse.",
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
                        "• Wash hands with soap after handling tobacco and before touching plants; disinfect tools with a bleach solution",
                "There is no cure for an infected plant. Remove and destroy infected plants (do not compost "
                        + "them), then disinfect tools and wash hands. Plant resistant varieties in the next crop.",
                "Virus",
                "Mottled light and dark green leaves, leaf distortion, stunting, uneven fruit ripening, and reduced fruit quality.",
                "Tomato mosaic virus → stable virus spread mechanically through sap, seed, tools, and handling."
        ).withNoTreatment());

        diseaseMap.put("Early Blight (Alternaria solani)", new DiseaseInfo(
                "A fungal disease of tomato and potato caused by Alternaria solani. Infection usually starts on older leaves near the ground and can affect leaves, stems, and fruits.",
                "Soilborne fungus that thrives in warm, humid, wet conditions. Spreads by wind, rain splash, contaminated tools, and crop debris.",
                "• Rotate crops for 2–3 years\n" +
                        "• Remove infected debris\n" +
                        "• Avoid overhead irrigation\n" +
                        "• Use resistant varieties\n" +
                        "• Apply mulch to reduce soil splash\n" +
                        "• Fungicide sprays (chlorothalonil, mancozeb) at first symptoms",
                "Remove infected lower leaves and keep foliage dry. Protectant fungicides such as chlorothalonil, "
                        + "mancozeb or copper, started at the first spots and repeated at the label interval, slow the "
                        + "disease. Rotating fungicide groups is recommended. Fungicides work best together with "
                        + "sanitation, mulching and crop rotation.",
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
                        "• Rotate crops, avoid potatoes nearby\n" +
                        "• Destroy volunteer potato and tomato plants",
                "Late blight can wipe out a field within days. Fungicides protect healthy plants but do not cure "
                        + "infected ones, so spray before the disease arrives or at the first sign during cool, wet "
                        + "weather. Metalaxyl-resistant strains are common, so rotate fungicide groups. Remove and bag "
                        + "infected plants and tell neighboring growers. Contact your agriculture office quickly if it appears.",
                "Oomycete (fungus-like)",
                "Leaves: Large, irregular water-soaked lesions; white mold underside.\n" +
                        "Stems: Dark brown-black lesions, may girdle stems.\n" +
                        "Fruits: Water-soaked brown lesions, white mold under wet conditions.",
                "Phytophthora infestans → spreads via wind and rain, thrives in cool humid weather, infects tomato & potato."
        ));

        diseaseMap.put("Anthracnose (Colletotrichum spp.)", new DiseaseInfo(
                "A fungal disease that mainly affects ripening tomato fruits, causing sunken, dark lesions and fruit rot.",
                "Caused by Colletotrichum spp. Spores spread mainly by rain splash and overhead irrigation; infected fruit and crop debris are important sources of inoculum.",
                "• Remove infected and fallen fruit promptly\n" +
                        "• Remove crop debris and practice crop rotation\n" +
                        "• Use mulch and avoid overhead irrigation\n" +
                        "• Harvest ripe fruit promptly\n" +
                        "• Use a protectant fungicide from fruit set only if its current Philippine label lists tomato and anthracnose",
                "Pick and destroy infected fruit and harvest ripe fruit promptly. Protectant fungicides such as chlorothalonil or mancozeb can reduce new infections when applied from fruit set and according to the label, but they do not heal existing sunken lesions. Sanitation, mulching, and avoiding overhead irrigation are key controls.",
                "Fungus",
                "Fruits: Small, sunken, dark lesions that may enlarge; salmon-colored spore masses may appear under wet conditions.\n" +
                        "Leaves: Less commonly, irregular necrotic spots and yellowing.\n" +
                        "Stems: Elongated dark lesions may occur.",
                "Colletotrichum spp. → Fungus that infects ripening fruit, survives in crop debris, and spreads mainly by splashing water."
        ));

        diseaseMap.put("Blossom End Rot", new DiseaseInfo(
                "A non-infectious physiological disorder causing a dark, sunken lesion at the blossom end of the fruit.",
                "Inconsistent water supply and poor calcium movement to developing fruit, often worsened by root stress.",
                "• Keep soil moisture even with mulch and regular irrigation\n" +
                        "• Avoid root damage and excessive nitrogen\n" +
                        "• Maintain balanced soil fertility\n" +
                        "• Do not rely on calcium sprays; consistent watering is the proven fix",
                "This is a physiological disorder, not an infection, so fungicides and insecticides do not "
                        + "help. Remove affected fruit and keep soil moisture steady. Mulch, avoid root damage, and avoid "
                        + "excess nitrogen. Affected fruit will not recover. Fixing watering protects new fruit. "
                        + "A soil test shows whether calcium is truly low.",
                "None",
                "Brown or black, firm, sunken lesion on the blossom end that may enlarge as the fruit develops.",
                "No pest causes this physiological disorder."
        ));

        diseaseMap.put("Late Blight (Phytophthora infestans)", new DiseaseInfo(
                "A highly destructive oomycete disease that can spread rapidly under cool, wet conditions and cause severe tomato yield loss if not managed early.",
                "Caused by Phytophthora infestans, an oomycete (water mold). It spreads rapidly through airborne spores during cool, humid, wet weather and can survive on infected plant material.",
                "• Use resistant varieties where available\n" +
                        "• Improve drainage and avoid overhead irrigation\n" +
                        "• Monitor frequently during cool, wet periods\n" +
                        "• Remove and destroy infected plants and volunteer tomato or potato plants\n" +
                        "• Begin a protectant fungicide program before favorable disease weather or at the first sign, following the label",
                "Late blight can spread very rapidly under favorable conditions. Fungicides protect healthy tissue but do not cure infected plants, so act early and use only products whose current Philippine label lists tomato and late blight. Rotate fungicide mode-of-action groups; resistance to metalaxyl-type products is a known concern. Remove severely infected plants, avoid working in wet foliage, and notify neighboring growers or the local agriculture office promptly.",
                "Oomycete (fungus-like)",
                "Leaves: Large, irregular, water-soaked lesions that become dark brown or black; white mold-like growth may appear on the leaf underside.\n" +
                        "Stems: Dark brown to black lesions that may girdle stems.\n" +
                        "Fruits: Brown, water-soaked lesions that can develop white mold-like growth under humid conditions.",
                "Phytophthora infestans → Oomycete spread by wind and rain splash; favored by cool, humid, wet weather and can infect tomato and potato."
        ));

        diseaseMap.put("Fruit Cracking", new DiseaseInfo(
                "A non-infectious physiological disorder in which tomato skin splits as fruit expands.",
                "Rapid fruit expansion after irregular watering, high humidity, or sudden changes in moisture.",
                "• Keep soil moisture consistent\n" +
                        "• Mulch and avoid severe pruning\n" +
                        "• Harvest promptly when fruit reaches maturity",
                "This is a physiological disorder with no treatment for cracked fruit. Harvest cracked fruit "
                        + "promptly to avoid rot and remove badly damaged ones. Keep irrigation steady, mulch, and "
                        + "harvest at the mature stage before heavy rain.",
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
                "This is a physiological disorder with no treatment for scalded fruit. Remove badly damaged "
                        + "fruit. Protect developing fruit by keeping leaf cover and using shade net during extreme "
                        + "heat. Avoid heavy pruning.",
                "None",
                "Pale yellow or white, firm patch on the sun-facing side that may become papery or necrotic.",
                "No pest causes this physiological disorder."
        ));

        diseaseMap.put("Fruit Worm (Fruit Borer)", new DiseaseInfo(
                "Fruit-feeding caterpillars bore into tomato fruit and damage the flesh and seeds.",
                "Larvae of several moth species, including Helicoverpa and other fruit-feeding caterpillars, may feed on flowers, leaves, and developing fruit.",
                "• Scout flowers and fruit regularly\n" +
                        "• Remove damaged fruit and crop debris\n" +
                        "• Hand-pick larvae and egg masses when practical\n" +
                        "• Use netting or biological controls where practical",
                "Hand-pick larvae and remove damaged fruit. Biological insecticides based on Bacillus thuringiensis (Bt) are most effective on young larvae when applied to foliage, flowers, and fruit. Once larvae enter the fruit, most products cannot reach them, so early detection and timing are critical. Use only FPA-registered insecticides, rotate modes of action, and observe the pre-harvest interval.",
                "Caterpillar",
                "Small entry holes, frass near the opening, internal feeding tunnels, and rotting fruit.",
                "Fruit-boring caterpillar → Moth larva that feeds inside tomato fruit and may leave dark droppings near entry holes."
        ));

        // TODO verify wording with advisers
        diseaseMap.put("Lady Beetle", new DiseaseInfo(
                "Many lady beetles are beneficial predators that eat aphids and other small pests. A few leaf-feeding species (Epilachna / 'peste ng talong') chew tomato leaves and can reduce yield if numbers are high.",
                "Adults and larvae of lady beetles; predatory species hunt aphids, while Epilachna beetles feed on foliage.",
                "• Watch plants weekly and identify the species before spraying\n" +
                        "• Conserve predatory lady beetles; avoid unnecessary insecticide\n" +
                        "• Hand-pick leaf-feeding beetles and larvae early in the morning\n" +
                        "• Keep weeds down around the plot",
                "Identify the species first. Do not spray predatory lady beetles, which protect your crop "
                        + "from aphids. For leaf-feeding Epilachna beetles, hand-pick adults, larvae and egg clusters "
                        + "early. Use a registered insecticide only if damage continues.",
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
                "Use yellow sticky traps and insecticidal soap or neem/horticultural oil on leaf undersides "
                        + "for light infestations. For heavy infestations use a registered insecticide, rotating modes "
                        + "of action because whiteflies resist products quickly. Remove virus-infected plants early. "
                        + "Control cannot reverse virus damage.",
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
                "Wash colonies off with a strong water spray, or use insecticidal soap or neem oil with good "
                        + "coverage. Conserve lady beetles, hoverflies and lacewings. Use a registered insecticide only "
                        + "if colonies keep returning, and avoid broad-spectrum products that remove natural enemies.",
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
                "Protect seedlings and young transplants with netting or row covers. Keep weeds down. If "
                        + "feeding is heavy on young plants, a registered insecticide can be used on seedlings. "
                        + "Established plants usually tolerate light damage.",
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
                "Remove and destroy badly mined leaves. Protect parasitic wasps that naturally control miners, "
                        + "so avoid broad-spectrum sprays. If damage continues, use a registered insecticide that "
                        + "reaches larvae inside the leaf, rotate products, and observe the pre-harvest interval.",
                "Insect",
                "• Serpentine white or silvery trails on leaves\n" +
                        "• Mines later turn brown and leaves may dry\n" +
                        "• Small maggots inside the leaf if a mine is opened",
                "Leaf miner → fly larva that tunnels inside the leaf, leaving winding mines."
        ));

        diseaseMap.put("Melon Fly", new DiseaseInfo(
                "A fruit fly that stings developing tomato fruit and lays eggs inside. Maggots feed in the flesh, causing rot, premature fruit drop, and unmarketable harvests.",
                "Adult melon flies (Zeugodacus cucurbitae, formerly Bactrocera cucurbitae) and related fruit flies puncture fruit to lay eggs; larvae develop inside the fruit.",
                "• Bag young fruits with paper or fine mesh where practical\n" +
                        "• Collect and destroy fallen and infested fruit daily\n" +
                        "• Use protein bait or lure traps around the field\n" +
                        "• Harvest promptly; do not leave ripe fruit on the plant",
                "Maggots inside fruit cannot be treated effectively. Remove and destroy infested fruit by deep burial or sealing it in a bag. Protect remaining fruit with bagging and traps using protein bait or lures. Use bait sprays only when the product is FPA-registered for the intended use and follow the label exactly.",
                "Insect",
                "Tiny puncture marks or soft spots on fruit; fruit that yellows, rots, or drops early; white maggots in the flesh when fruit is cut.",
                "Melon fly → Fruit fly that lays eggs in tomato fruit; larvae feed inside and spoil the harvest."
        ));

        diseaseMap.put("Worms", new DiseaseInfo(
                "Moth larvae (fruitworms, armyworms, hornworms, and leaf-feeding caterpillars) chew tomato leaves, flowers, and fruit. Fruit borers hide inside the fruit and cause rotting.",
                "Eggs are laid on leaves or near flowers; larvae feed at night or inside fruit. Outbreaks often follow flowering and fruit set.",
                "• Scout leaves, flowers, and fruit at least twice a week\n" +
                        "• Hand-pick larvae and egg masses in the morning\n" +
                        "• Remove damaged fruit so larvae cannot finish development\n" +
                        "• Encourage birds and beneficial wasps; avoid blanket spraying",
                "Hand-pick larvae and egg masses. Bt sprays work well on young caterpillars when applied to "
                        + "foliage, flowers and fruit. Older larvae and fruit borers are harder to control. Use only "
                        + "registered insecticides, rotate modes of action, and observe the pre-harvest interval.",
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