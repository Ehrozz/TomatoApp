package com.android.tomatoapp.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhilippineLocations {
    
    // Region 1 (Ilocos Region) - All cities and municipalities
    public static final String[] REGION_1_LABELS = {
            // Ilocos Norte
            "Laoag, Ilocos Norte",
            "Batac, Ilocos Norte",
            "San Nicolas, Ilocos Norte",
            "Sarrat, Ilocos Norte",
            "Piddig, Ilocos Norte",
            "Solsona, Ilocos Norte",
            "Dingras, Ilocos Norte",
            "Banna, Ilocos Norte",
            "Marcos, Ilocos Norte",
            "Nueva Era, Ilocos Norte",
            "Badoc, Ilocos Norte",
            "Pinili, Ilocos Norte",
            "Currimao, Ilocos Norte",
            "Paoay, Ilocos Norte",
            "Pasuquin, Ilocos Norte",
            "Burgos, Ilocos Norte",
            "Bacarra, Ilocos Norte",
            "Vintar, Ilocos Norte",
            "Dumalneg, Ilocos Norte",
            "Adams, Ilocos Norte",
            "Pagudpud, Ilocos Norte",
            "Carasi, Ilocos Norte",
            // Ilocos Sur
            "Vigan, Ilocos Sur",
            "Santa, Ilocos Sur",
            "Bantay, Ilocos Sur",
            "San Ildefonso, Ilocos Sur",
            "San Vicente, Ilocos Sur",
            "Magsingal, Ilocos Sur",
            "Santo Domingo, Ilocos Sur",
            "San Juan, Ilocos Sur",
            "Cabugao, Ilocos Sur",
            "Sinait, Ilocos Sur",
            "San Esteban, Ilocos Sur",
            "Santiago, Ilocos Sur",
            "Candon, Ilocos Sur",
            "Cervantes, Ilocos Sur",
            "Lidlidda, Ilocos Sur",
            "San Emilio, Ilocos Sur",
            "Nagbukel, Ilocos Sur",
            "Galimuyod, Ilocos Sur",
            "Salcedo, Ilocos Sur",
            "Sigay, Ilocos Sur",
            "Tagudin, Ilocos Sur",
            "Suyo, Ilocos Sur",
            "Alilem, Ilocos Sur",
            "Sugpon, Ilocos Sur",
            "Banayoyo, Ilocos Sur",
            "San Sebastian, Ilocos Sur",
            "Quirino, Ilocos Sur",
            "Gregorio del Pilar, Ilocos Sur",
            // La Union
            "San Fernando, La Union",
            "Bacnotan, La Union",
            "San Juan, La Union",
            "Bauang, La Union",
            "Agoo, La Union",
            "Aringay, La Union",
            "Caba, La Union",
            "Tubao, La Union",
            "Pugo, La Union",
            "Rosario, La Union",
            "Santo Tomas, La Union",
            "Naguilian, La Union",
            "Burgos, La Union",
            "Bagulin, La Union",
            "San Gabriel, La Union",
            "Tublay, La Union",
            "Santol, La Union",
            "Sudipen, La Union",
            "Luna, La Union",
            // Pangasinan
            "Dagupan, Pangasinan",
            "San Carlos, Pangasinan",
            "Urdaneta, Pangasinan",
            "Alaminos, Pangasinan",
            "Lingayen, Pangasinan",
            "Mangaldan, Pangasinan",
            "Calasiao, Pangasinan",
            "Malasiqui, Pangasinan",
            "Bayambang, Pangasinan",
            "Binmaley, Pangasinan",
            "Manaoag, Pangasinan",
            "Pozorrubio, Pangasinan",
            "Tayug, Pangasinan",
            "Asingan, Pangasinan",
            "Villasis, Pangasinan",
            "Bautista, Pangasinan",
            "Alcala, Pangasinan",
            "Bolinao, Pangasinan",
            "Sual, Pangasinan",
            "Labrador, Pangasinan",
            "Bugallon, Pangasinan",
            "Aguilar, Pangasinan",
            "Mangatarem, Pangasinan",
            "Infanta, Pangasinan",
            "Dasol, Pangasinan",
            "Burgos, Pangasinan",
            "Bani, Pangasinan",
            "Agno, Pangasinan",
            "Anda, Pangasinan",
            "Mapandan, Pangasinan",
            "Sta. Barbara, Pangasinan",
            "San Jacinto, Pangasinan",
            "San Fabian, Pangasinan",
            "San Manuel, Pangasinan",
            "Sison, Pangasinan",
            "Binalonan, Pangasinan",
            "Laoac, Pangasinan",
            "Umingan, Pangasinan",
            "San Quintin, Pangasinan",
            "Natividad, Pangasinan",
            "Sta. Maria, Pangasinan",
            "Balungao, Pangasinan",
            "Rosales, Pangasinan",
            "Basista, Pangasinan",
            "San Nicolas, Pangasinan",
            "Sto. Tomas, Pangasinan"
    };

    public static final double[] REGION_1_LATS = {
            // Ilocos Norte
            18.1989, 18.0564, 18.1699, 18.1519, 18.0569, 18.0969, 18.0647, 17.9839, 18.0409, 17.9169,
            17.9269, 17.9509, 18.0129, 18.0639, 18.3309, 18.5159, 18.2499, 18.0409, 18.4709, 18.4579,
            18.5839, 18.1239,
            // Ilocos Sur
            17.5747, 17.4847, 17.5847, 17.5847, 17.6047, 17.6847, 17.6347, 17.6547, 17.7947, 17.8147,
            17.8647, 17.3147, 17.2947, 17.1947, 16.9850, 17.0847, 17.2447, 17.1747, 17.1147, 17.1247,
            17.0347, 16.9797, 16.8847, 16.8247, 17.3247, 17.2347, 17.1547, 17.1547,
            // La Union
            16.6159, 16.7239, 16.6819, 16.5199, 16.3289, 16.3839, 16.4179, 16.3449, 16.3199, 16.2299,
            16.2799, 16.5349, 16.5449, 16.6079, 16.6299, 16.4499, 16.6679, 16.7589, 16.8539,
            // Pangasinan
            16.0431, 15.9261, 15.9761, 16.1551, 16.0211, 16.0691, 15.9911, 15.9781, 15.7981, 16.0331,
            16.0441, 16.1161, 16.0271, 16.0031, 15.9011, 15.8111, 15.8411, 16.3921, 16.0891, 16.0371,
            15.9731, 15.8291, 15.7691, 15.8391, 15.9291, 16.2091, 16.1991, 16.1191, 16.2891, 15.9991,
            16.0091, 15.9991, 16.0691, 16.1291, 15.9391, 16.1691, 16.0391, 16.0391, 15.9791, 15.9991,
            16.0391, 15.9491, 15.9091, 16.0591, 15.9791, 15.9891, 15.9991, 16.0091
    };

    public static final double[] REGION_1_LONS = {
            // Ilocos Norte
            120.5920, 120.5640, 120.5890, 120.5790, 120.5630, 120.7780, 120.7070, 120.6290, 120.6890, 120.8280,
            120.4890, 120.5190, 120.5190, 120.5290, 120.6190, 120.6490, 120.6090, 120.6390, 120.7090, 120.8990,
            120.7590, 120.8590,
            // Ilocos Sur
            120.3869, 120.3869, 120.3969, 120.4069, 120.5269, 120.5769, 120.5869, 120.6069, 120.6369, 120.6469,
            120.6569, 120.6969, 120.7069, 120.5769, 120.7069, 120.7369, 120.7469, 120.7569, 120.7669, 120.7769,
            120.7869, 120.7969, 120.8269, 120.8569, 120.5369, 120.5569, 120.5769, 120.5869,
            // La Union
            120.3159, 120.3539, 120.3739, 120.3339, 120.3659, 120.2959, 120.3359, 120.3259, 120.5059, 120.4859,
            120.3859, 120.3859, 120.4659, 120.3959, 120.4059, 120.4559, 120.4159, 120.4659, 120.4159,
            // Pangasinan
            120.3431, 120.3531, 120.5711, 119.9831, 120.2311, 120.4031, 120.3611, 120.4211, 120.4511, 120.2611,
            120.4811, 120.5311, 120.7311, 120.6711, 120.5911, 120.5111, 120.5211, 119.9131, 120.0811, 120.1411,
            120.2111, 120.1211, 120.2911, 120.3011, 120.2611, 120.4211, 120.4311, 120.0111, 119.9511, 120.3711,
            120.3811, 120.4011, 120.4211, 120.4311, 120.3511, 120.4611, 120.4711, 120.4811, 120.5111, 120.5211,
            120.5311, 120.5411, 120.5511, 120.5611, 120.5711, 120.5811, 120.5911, 120.6011
    };

    // Region 2 (Cagayan Valley) - All cities and municipalities
    public static final String[] REGION_2_LABELS = {
            // Batanes
            "Basco, Batanes",
            "Itbayat, Batanes",
            "Ivana, Batanes",
            "Mahatao, Batanes",
            "Sabtang, Batanes",
            "Uyugan, Batanes",
            // Cagayan
            "Tuguegarao, Cagayan",
            "Abulug, Cagayan",
            "Alcala, Cagayan",
            "Allacapan, Cagayan",
            "Amulung, Cagayan",
            "Aparri, Cagayan",
            "Baggao, Cagayan",
            "Ballesteros, Cagayan",
            "Buguey, Cagayan",
            "Calayan, Cagayan",
            "Camalaniugan, Cagayan",
            "Claveria, Cagayan",
            "Enrile, Cagayan",
            "Gattaran, Cagayan",
            "Gonzaga, Cagayan",
            "Iguig, Cagayan",
            "Lal-lo, Cagayan",
            "Lasam, Cagayan",
            "Pamplona, Cagayan",
            "Peñablanca, Cagayan",
            "Piat, Cagayan",
            "Rizal, Cagayan",
            "Sanchez-Mira, Cagayan",
            "Santa Ana, Cagayan",
            "Santa Praxedes, Cagayan",
            "Santa Teresita, Cagayan",
            "Santo Niño, Cagayan",
            "Solana, Cagayan",
            "Tuao, Cagayan",
            // Isabela
            "Ilagan, Isabela",
            "Cauayan, Isabela",
            "Santiago, Isabela",
            "Alicia, Isabela",
            "Angadanan, Isabela",
            "Aurora, Isabela",
            "Benito Soliven, Isabela",
            "Burgos, Isabela",
            "Cabagan, Isabela",
            "Cabatuan, Isabela",
            "Cordon, Isabela",
            "Delfin Albano, Isabela",
            "Dinapigue, Isabela",
            "Divilacan, Isabela",
            "Echague, Isabela",
            "Gamu, Isabela",
            "Jones, Isabela",
            "Luna, Isabela",
            "Maconacon, Isabela",
            "Mallig, Isabela",
            "Naguilian, Isabela",
            "Palanan, Isabela",
            "Quezon, Isabela",
            "Quirino, Isabela",
            "Ramon, Isabela",
            "Reina Mercedes, Isabela",
            "Roxas, Isabela",
            "San Agustin, Isabela",
            "San Guillermo, Isabela",
            "San Isidro, Isabela",
            "San Manuel, Isabela",
            "San Mariano, Isabela",
            "San Mateo, Isabela",
            "San Pablo, Isabela",
            "Santa Maria, Isabela",
            "Santo Tomas, Isabela",
            "Tumauini, Isabela",
            // Nueva Vizcaya
            "Bayombong, Nueva Vizcaya",
            "Alfonso Castaneda, Nueva Vizcaya",
            "Ambaguio, Nueva Vizcaya",
            "Aritao, Nueva Vizcaya",
            "Bagabag, Nueva Vizcaya",
            "Bambang, Nueva Vizcaya",
            "Diadi, Nueva Vizcaya",
            "Dupax del Norte, Nueva Vizcaya",
            "Dupax del Sur, Nueva Vizcaya",
            "Kasibu, Nueva Vizcaya",
            "Kayapa, Nueva Vizcaya",
            "Quezon, Nueva Vizcaya",
            "Santa Fe, Nueva Vizcaya",
            "Solano, Nueva Vizcaya",
            "Villaverde, Nueva Vizcaya",
            // Quirino
            "Cabarroguis, Quirino",
            "Aglipay, Quirino",
            "Maddela, Quirino",
            "Nagtipunan, Quirino",
            "Saguday, Quirino",
            "Diffun, Quirino"
    };

    public static final double[] REGION_2_LATS = {
            // Batanes
            20.4487, 20.7853, 20.3710, 20.4170, 20.3310, 20.3900,
            // Cagayan
            17.6159, 18.4459, 17.9019, 18.2259, 17.8319, 18.3639, 18.3619, 18.4079, 18.2709, 19.3059,
            18.2739, 18.4759, 17.5759, 18.2539, 18.2289, 17.6199, 18.6109, 17.6199, 17.6199, 18.2539,
            18.2539, 18.2539, 18.2539, 18.2539, 18.2539, 17.6199, 18.2539, 18.2539, 18.2539, 18.2539,
            18.2539, 18.2539, 18.2539, 18.2539, 18.2539, 18.2539, 18.2539,
            // Isabela
            17.1489, 16.9339, 16.6919, 16.7699, 16.7799, 16.9899, 16.9799, 17.0099, 17.7899, 16.9599,
            16.6399, 17.5099, 16.5199, 17.3499, 16.6999, 17.0099, 16.5999, 16.9299, 17.3799, 17.0099,
            16.5299, 17.0099, 17.0099, 16.5299, 16.7799, 16.8499, 16.7799, 16.5199, 16.7799, 16.7799,
            16.7799, 16.7799, 16.7799, 17.0099, 16.7799, 17.0099, 17.0099,
            // Nueva Vizcaya
            16.4814, 16.1524, 16.3324, 16.2944, 16.6084, 16.3874, 16.6464, 16.2944, 16.2944, 16.2944,
            16.2944, 16.2944, 16.2944, 16.2944, 16.2944,
            // Quirino
            16.5824, 16.4684, 16.3414, 16.2154, 16.5284, 16.5824
    };

    public static final double[] REGION_2_LONS = {
            // Batanes
            121.9700, 121.8410, 121.9010, 121.9470, 121.8710, 121.9370,
            // Cagayan
            121.7269, 121.4549, 121.6549, 121.1849, 121.7249, 121.6549, 121.8849, 121.4949, 121.8349, 121.9249,
            121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549,
            121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549,
            121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549, 121.6549,
            // Isabela
            121.8889, 121.7689, 121.5489, 121.6989, 121.6989, 121.6389, 121.6989, 121.6989, 121.6989, 121.6989,
            121.6989, 121.6989, 122.2889, 122.2989, 121.6989, 121.6989, 121.6989, 121.6989, 122.2989, 121.6989,
            122.2889, 121.6989, 121.6989, 122.2889, 121.6989, 121.6989, 121.6989, 122.2889, 121.6989, 121.6989,
            121.6989, 121.6989, 121.6989, 121.6989, 121.6989, 121.6989, 121.6989,
            // Nueva Vizcaya
            121.1575, 121.2375, 121.1575, 121.2375, 121.1575, 121.2375, 121.1575, 121.2375, 121.2375, 121.2375,
            121.2375, 121.2375, 121.2375, 121.1575, 121.1575,
            // Quirino
            121.5175, 121.5375, 121.5175, 121.5375, 121.5175, 121.5175
    };

    /**
     * Get all location labels for Region 1
     */
    public static String[] getRegion1Labels() {
        return REGION_1_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 1
     */
    public static double[] getRegion1Lats() {
        return REGION_1_LATS;
    }

    /**
     * Get all longitude coordinates for Region 1
     */
    public static double[] getRegion1Lons() {
        return REGION_1_LONS;
    }

    /**
     * Get location data for a specific index in Region 1
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion1Location(int index) {
        if (index >= 0 && index < REGION_1_LABELS.length) {
            return new LocationData(
                    REGION_1_LABELS[index],
                    REGION_1_LATS[index],
                    REGION_1_LONS[index]
            );
        }
        return null;
    }

    /**
     * Get all location labels for Region 2
     */
    public static String[] getRegion2Labels() {
        return REGION_2_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 2
     */
    public static double[] getRegion2Lats() {
        return REGION_2_LATS;
    }

    /**
     * Get all longitude coordinates for Region 2
     */
    public static double[] getRegion2Lons() {
        return REGION_2_LONS;
    }

    /**
     * Get location data for a specific index in Region 2
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion2Location(int index) {
        if (index >= 0 && index < REGION_2_LABELS.length) {
            return new LocationData(
                    REGION_2_LABELS[index],
                    REGION_2_LATS[index],
                    REGION_2_LONS[index]
            );
        }
        return null;
    }

    // Region 3 (Central Luzon) - All cities and municipalities
    public static final String[] REGION_3_LABELS = {
            // Aurora
            "Baler, Aurora",
            "Casiguran, Aurora",
            "Dilasag, Aurora",
            "Dinalungan, Aurora",
            "Dingalan, Aurora",
            "Dipaculao, Aurora",
            "Maria Aurora, Aurora",
            "San Luis, Aurora",
            // Bataan
            "Balanga, Bataan",
            "Abucay, Bataan",
            "Bagac, Bataan",
            "Dinalupihan, Bataan",
            "Hermosa, Bataan",
            "Limay, Bataan",
            "Mariveles, Bataan",
            "Morong, Bataan",
            "Orani, Bataan",
            "Orion, Bataan",
            "Pilar, Bataan",
            "Samal, Bataan",
            // Bulacan
            "Malolos, Bulacan",
            "Meycauayan, Bulacan",
            "San Jose del Monte, Bulacan",
            "Angat, Bulacan",
            "Balagtas, Bulacan",
            "Baliuag, Bulacan",
            "Bocaue, Bulacan",
            "Bulakan, Bulacan",
            "Bustos, Bulacan",
            "Calumpit, Bulacan",
            "Doña Remedios Trinidad, Bulacan",
            "Guiguinto, Bulacan",
            "Hagonoy, Bulacan",
            "Marilao, Bulacan",
            "Norzagaray, Bulacan",
            "Obando, Bulacan",
            "Pandi, Bulacan",
            "Paombong, Bulacan",
            "Plaridel, Bulacan",
            "Pulilan, Bulacan",
            "San Ildefonso, Bulacan",
            "San Miguel, Bulacan",
            "San Rafael, Bulacan",
            "Santa Maria, Bulacan",
            // Nueva Ecija
            "Palayan, Nueva Ecija",
            "Cabanatuan, Nueva Ecija",
            "Gapan, Nueva Ecija",
            "San Jose, Nueva Ecija",
            "Science City of Muñoz, Nueva Ecija",
            "Aliaga, Nueva Ecija",
            "Bongabon, Nueva Ecija",
            "Cabiao, Nueva Ecija",
            "Carranglan, Nueva Ecija",
            "Cuyapo, Nueva Ecija",
            "Gabaldon, Nueva Ecija",
            "General Mamerto Natividad, Nueva Ecija",
            "General Tinio, Nueva Ecija",
            "Guimba, Nueva Ecija",
            "Jaen, Nueva Ecija",
            "Laur, Nueva Ecija",
            "Llanera, Nueva Ecija",
            "Lupao, Nueva Ecija",
            "Nampicuan, Nueva Ecija",
            "Pantabangan, Nueva Ecija",
            "Peñaranda, Nueva Ecija",
            "Quezon, Nueva Ecija",
            "Rizal, Nueva Ecija",
            "San Antonio, Nueva Ecija",
            "San Isidro, Nueva Ecija",
            "San Leonardo, Nueva Ecija",
            "Santa Rosa, Nueva Ecija",
            "Santo Domingo, Nueva Ecija",
            "Talavera, Nueva Ecija",
            "Talugtug, Nueva Ecija",
            "Zaragoza, Nueva Ecija",
            // Pampanga
            "Angeles, Pampanga",
            "San Fernando, Pampanga",
            "Apalit, Pampanga",
            "Arayat, Pampanga",
            "Bacolor, Pampanga",
            "Candaba, Pampanga",
            "Floridablanca, Pampanga",
            "Guagua, Pampanga",
            "Lubao, Pampanga",
            "Mabalacat, Pampanga",
            "Macabebe, Pampanga",
            "Magalang, Pampanga",
            "Masantol, Pampanga",
            "Mexico, Pampanga",
            "Minalin, Pampanga",
            "Porac, Pampanga",
            "San Luis, Pampanga",
            "San Simon, Pampanga",
            "Santa Ana, Pampanga",
            "Santa Rita, Pampanga",
            "Santo Tomas, Pampanga",
            // Tarlac
            "Tarlac City, Tarlac",
            "Anao, Tarlac",
            "Bamban, Tarlac",
            "Camiling, Tarlac",
            "Capas, Tarlac",
            "Concepcion, Tarlac",
            "Gerona, Tarlac",
            "La Paz, Tarlac",
            "Mayantoc, Tarlac",
            "Moncada, Tarlac",
            "Paniqui, Tarlac",
            "Pura, Tarlac",
            "Ramos, Tarlac",
            "San Clemente, Tarlac",
            "San Jose, Tarlac",
            "San Manuel, Tarlac",
            "Santa Ignacia, Tarlac",
            "Victoria, Tarlac",
            // Zambales
            "Olongapo, Zambales",
            "Botolan, Zambales",
            "Cabangan, Zambales",
            "Candelaria, Zambales",
            "Castillejos, Zambales",
            "Iba, Zambales",
            "Masinloc, Zambales",
            "Palauig, Zambales",
            "San Antonio, Zambales",
            "San Felipe, Zambales",
            "San Marcelino, Zambales",
            "San Narciso, Zambales",
            "Santa Cruz, Zambales",
            "Subic, Zambales"
    };

    public static final double[] REGION_3_LATS = {
            // Aurora
            15.7580, 16.2640, 16.3970, 16.0810, 15.3480, 15.5780, 15.7890, 15.7270,
            // Bataan
            14.6760, 14.7200, 14.5950, 14.8800, 14.8260, 14.5640, 14.4340, 14.6730, 14.8040, 14.6180,
            14.6640, 14.7660,
            // Bulacan
            14.8527, 14.7367, 14.8137, 15.0017, 14.8287, 14.9547, 14.7967, 14.7937, 14.9537, 14.9187,
            15.0017, 14.8317, 14.8317, 14.7587, 14.9117, 14.9017, 14.8747, 14.9017, 14.9017, 14.9017,
            14.9017, 15.0787, 15.1457, 14.9017, 14.8187,
            // Nueva Ecija
            15.5407, 15.4867, 15.3117, 15.7927, 15.7107, 15.5017, 15.6327, 15.2467, 15.9617, 15.7877,
            15.4577, 15.6017, 15.3507, 15.6587, 15.3357, 15.5857, 15.6647, 15.8777, 15.7317, 15.8337,
            15.8337, 15.7017, 15.7017, 15.7017, 15.7017, 15.7017, 15.7017, 15.7017, 15.7017, 15.7017,
            15.7017,
            // Pampanga
            15.1467, 15.0317, 14.9537, 15.1497, 14.9967, 15.0957, 14.9777, 14.9667, 14.9317, 15.2217,
            14.9087, 15.2157, 14.9087, 15.0617, 14.9677, 15.0447, 15.0407, 14.9677, 15.0957, 14.9677,
            14.9677,
            // Tarlac
            15.4867, 15.7307, 15.2037, 15.6867, 15.3347, 15.3257, 15.6057, 15.4437, 15.6197, 15.7357,
            15.6687, 15.6247, 15.6687, 15.7137, 15.7917, 15.7917, 15.6137, 15.5787,
            // Zambales
            14.8386, 15.2896, 15.1586, 15.6686, 14.9346, 15.3276, 15.5366, 15.4336, 14.9496, 15.0616,
            14.9796, 15.0166, 15.7346, 14.8796
    };

    public static final double[] REGION_3_LONS = {
            // Aurora
            121.5663, 122.1603, 122.2543, 121.6953, 121.3923, 121.6323, 121.4833, 121.5103,
            // Bataan
            120.5360, 120.5360, 120.3920, 120.4640, 120.5080, 120.5540, 120.5080, 120.2660, 120.5360, 120.5360,
            120.5360, 120.5360,
            // Bulacan
            120.8185, 120.9535, 121.0455, 121.0265, 121.0765, 120.9015, 120.9265, 120.8785, 121.0765, 120.7685,
            121.0765, 120.8785, 120.8785, 120.8785, 121.0765, 121.0265, 120.8785, 121.0265, 120.8785, 120.8785,
            120.8785, 121.0765, 121.0265, 120.8785, 120.9785,
            // Nueva Ecija
            121.1085, 120.9685, 120.7575, 120.9925, 120.9035, 120.8435, 120.7585, 121.0635, 121.0635, 120.6685,
            121.3385, 121.0635, 121.0385, 120.7585, 120.8435, 121.2135, 121.0635, 120.9035, 120.7585, 121.0635,
            121.0635, 120.7585, 120.7585, 120.7585, 120.7585, 120.7585, 120.7585, 120.7585, 120.7585, 120.7585,
            120.7585,
            // Pampanga
            120.5905, 120.6865, 120.7655, 120.7825, 120.6535, 120.8275, 120.5085, 120.6325, 120.6015, 120.5815,
            120.7155, 120.6615, 120.7155, 120.6675, 120.6995, 120.5425, 120.7925, 120.6995, 120.8275, 120.6995,
            120.6995,
            // Tarlac
            120.5965, 120.6475, 120.3315, 120.4205, 120.5945, 120.6545, 120.5975, 120.7305, 120.3815, 120.5415,
            120.5805, 120.5805, 120.5805, 120.3565, 120.7945, 120.7945, 120.4565, 120.6665,
            // Zambales
            120.2842, 120.0242, 120.1552, 119.9702, 120.3152, 119.9792, 119.9172, 119.9052, 120.0862, 120.0662,
            120.1562, 119.9662, 119.9642, 120.2392
    };

    /**
     * Get all location labels for Region 3
     */
    public static String[] getRegion3Labels() {
        return REGION_3_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 3
     */
    public static double[] getRegion3Lats() {
        return REGION_3_LATS;
    }

    /**
     * Get all longitude coordinates for Region 3
     */
    public static double[] getRegion3Lons() {
        return REGION_3_LONS;
    }

    /**
     * Get location data for a specific index in Region 3
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion3Location(int index) {
        if (index >= 0 && index < REGION_3_LABELS.length) {
            return new LocationData(
                    REGION_3_LABELS[index],
                    REGION_3_LATS[index],
                    REGION_3_LONS[index]
            );
        }
        return null;
    }

    // Region 4 (CALABARZON) - All cities and municipalities
    public static final String[] REGION_4_LABELS = {
            // Cavite
            "Dasmariñas, Cavite",
            "Tagaytay, Cavite",
            "Bacoor, Cavite",
            "Imus, Cavite",
            "Cavite City, Cavite",
            "General Trias, Cavite",
            "Trece Martires, Cavite",
            "Alfonso, Cavite",
            "Amadeo, Cavite",
            "Carmona, Cavite",
            "General Emilio Aguinaldo, Cavite",
            "General Mariano Alvarez, Cavite",
            "Indang, Cavite",
            "Kawit, Cavite",
            "Magallanes, Cavite",
            "Maragondon, Cavite",
            "Mendez, Cavite",
            "Naic, Cavite",
            "Noveleta, Cavite",
            "Rosario, Cavite",
            "Silang, Cavite",
            "Tanza, Cavite",
            "Ternate, Cavite",
            // Laguna
            "Calamba, Laguna",
            "San Pablo, Laguna",
            "Santa Rosa, Laguna",
            "Biñan, Laguna",
            "Cabuyao, Laguna",
            "San Pedro, Laguna",
            "Alaminos, Laguna",
            "Bay, Laguna",
            "Calauan, Laguna",
            "Cavinti, Laguna",
            "Famy, Laguna",
            "Kalayaan, Laguna",
            "Liliw, Laguna",
            "Los Baños, Laguna",
            "Luisiana, Laguna",
            "Lumban, Laguna",
            "Mabitac, Laguna",
            "Magdalena, Laguna",
            "Majayjay, Laguna",
            "Nagcarlan, Laguna",
            "Paete, Laguna",
            "Pagsanjan, Laguna",
            "Pakil, Laguna",
            "Pangil, Laguna",
            "Pila, Laguna",
            "Rizal, Laguna",
            "San Antonio, Laguna",
            "Santa Cruz, Laguna",
            "Santa Maria, Laguna",
            "Siniloan, Laguna",
            "Victoria, Laguna",
            // Batangas
            "Batangas City, Batangas",
            "Lipa, Batangas",
            "Tanauan, Batangas",
            "Calapan, Batangas",
            "Agoncillo, Batangas",
            "Alitagtag, Batangas",
            "Balayan, Batangas",
            "Balete, Batangas",
            "Bauan, Batangas",
            "Calaca, Batangas",
            "Calatagan, Batangas",
            "Cuenca, Batangas",
            "Ibaan, Batangas",
            "Laurel, Batangas",
            "Lemery, Batangas",
            "Lian, Batangas",
            "Lobo, Batangas",
            "Mabini, Batangas",
            "Malvar, Batangas",
            "Mataasnakahoy, Batangas",
            "Nasugbu, Batangas",
            "Padre Garcia, Batangas",
            "Rosario, Batangas",
            "San Jose, Batangas",
            "San Juan, Batangas",
            "San Luis, Batangas",
            "San Nicolas, Batangas",
            "San Pascual, Batangas",
            "Santa Teresita, Batangas",
            "Santo Tomas, Batangas",
            "Taal, Batangas",
            "Talisay, Batangas",
            "Taysan, Batangas",
            "Tingloy, Batangas",
            "Tuy, Batangas",
            // Rizal
            "Antipolo, Rizal",
            "Angono, Rizal",
            "Baras, Rizal",
            "Binangonan, Rizal",
            "Cainta, Rizal",
            "Cardona, Rizal",
            "Jalajala, Rizal",
            "Morong, Rizal",
            "Pililla, Rizal",
            "Rodriguez, Rizal",
            "San Mateo, Rizal",
            "Tanay, Rizal",
            "Taytay, Rizal",
            "Teresa, Rizal",
            // Quezon
            "Lucena, Quezon",
            "Tayabas, Quezon",
            "Agdangan, Quezon",
            "Alabat, Quezon",
            "Atimonan, Quezon",
            "Buenavista, Quezon",
            "Burdeos, Quezon",
            "Calauag, Quezon",
            "Candelaria, Quezon",
            "Catanauan, Quezon",
            "Dolores, Quezon",
            "General Luna, Quezon",
            "General Nakar, Quezon",
            "Guinayangan, Quezon",
            "Gumaca, Quezon",
            "Infanta, Quezon",
            "Jomalig, Quezon",
            "Lopez, Quezon",
            "Lucban, Quezon",
            "Macalelon, Quezon",
            "Mauban, Quezon",
            "Mulanay, Quezon",
            "Padre Burgos, Quezon",
            "Pagbilao, Quezon",
            "Panukulan, Quezon",
            "Patnanungan, Quezon",
            "Perez, Quezon",
            "Pitogo, Quezon",
            "Plaridel, Quezon",
            "Polillo, Quezon",
            "Quezon, Quezon",
            "Real, Quezon",
            "Sampaloc, Quezon",
            "San Andres, Quezon",
            "San Antonio, Quezon",
            "San Francisco, Quezon",
            "San Narciso, Quezon",
            "Sariaya, Quezon",
            "Tagkawayan, Quezon",
            "Tiaong, Quezon",
            "Unisan, Quezon"
    };

    public static final double[] REGION_4_LATS = {
            // Cavite
            14.3294, 14.1004, 14.4584, 14.4294, 14.4814, 14.3854, 14.2804, 14.1404, 14.1694, 14.3204,
            14.1844, 14.3074, 14.2494, 14.4444, 14.3854, 14.2744, 14.1294, 14.3244, 14.4294, 14.4144,
            14.2304, 14.3944, 14.2894,
            // Laguna
            14.2115, 14.0685, 14.3125, 14.3385, 14.2835, 14.3585, 14.1555, 14.1835, 14.1495, 14.2135,
            14.2435, 14.2175, 14.3235, 14.3235, 14.1345, 14.1655, 14.2985, 14.2985, 14.4295, 14.1995,
            14.1455, 14.1995, 14.2735, 14.3865, 14.4025, 14.2375, 14.1085, 14.1495, 14.2835, 14.4195,
            14.1995, 14.2235,
            // Batangas
            13.7565, 13.9415, 14.0855, 13.4125, 13.9365, 13.8665, 13.9365, 13.9865, 13.7915, 13.9565,
            13.8315, 13.9065, 13.8215, 14.0515, 13.9365, 13.9565, 13.6565, 13.7065, 14.0515, 13.9365,
            14.0515, 13.9365, 13.8465, 13.8815, 13.8815, 13.8365, 13.8815, 13.8815, 13.8815, 14.0515,
            13.8815, 14.0915, 13.8815, 13.6565, 13.8815, 14.0515,
            // Rizal
            14.6255, 14.5255, 14.5255, 14.4555, 14.5785, 14.4865, 14.3565, 14.5115, 14.4815, 14.7615,
            14.6915, 14.5365, 14.5695, 14.5565,
            // Quezon
            13.9311, 14.0291, 13.8761, 14.1011, 14.0011, 13.7261, 14.8461, 13.9561, 13.9311, 13.5911,
            14.2681, 13.6911, 14.7611, 13.9011, 13.9261, 14.7361, 14.6961, 13.8840, 14.1131, 13.8511,
            14.1911, 13.5211, 13.9211, 13.9661, 14.6911, 14.7911, 14.1911, 13.8211, 13.9511, 14.7211,
            14.0011, 14.6611, 14.1861, 13.8211, 13.8511, 13.8511, 13.8811, 13.8761, 13.8811, 13.9561,
            13.8411
    };

    public static final double[] REGION_4_LONS = {
            // Cavite
            120.9364, 120.9334, 120.8984, 120.9364, 120.8994, 120.8814, 120.8564, 120.8564, 120.9184, 121.0564,
            120.8064, 121.0014, 120.8844, 120.9014, 120.7414, 120.7764, 120.9064, 120.7914, 120.8714, 120.8564,
            121.0564, 120.7914, 120.7214,
            // Laguna
            121.1655, 121.3255, 121.0565, 121.1265, 121.1265, 121.0565, 121.2465, 121.2865, 121.2465, 121.2465,
            121.2865, 121.2465, 121.2865, 121.2465, 121.2865, 121.2465, 121.5065, 121.2865, 121.2865, 121.2865,
            121.2865, 121.2865, 121.2865, 121.2865, 121.2865, 121.2865, 121.2865, 121.2865, 121.2865, 121.2865,
            121.2865, 121.2865,
            // Batangas
            121.0584, 121.1634, 121.0884, 121.1803, 120.9284, 120.9284, 120.7284, 121.0284, 120.9884, 120.7984,
            120.5984, 121.0284, 121.0284, 120.9884, 120.8984, 120.5984, 121.2584, 120.8984, 121.0584, 121.0284,
            120.6384, 121.0284, 120.8584, 121.0984, 121.0584, 121.0584, 120.9984, 121.0284, 121.0284, 121.0584,
            120.9984, 120.9984, 121.0284, 121.2584, 121.0284, 121.0584,
            // Rizal
            121.1245, 121.1545, 121.2645, 121.1945, 121.1245, 121.2245, 121.3545, 121.2345, 121.3045, 121.1945,
            121.1245, 121.3245, 121.1245, 121.2345,
            // Quezon
            121.6130, 121.5930, 121.6130, 122.1530, 121.9230, 121.6130, 122.1030, 122.2930, 121.4230, 122.3230,
            121.4030, 122.1730, 121.6130, 122.4930, 122.3730, 121.6130, 121.6130, 122.2633, 121.5530, 122.1030,
            121.7730, 122.3730, 121.8430, 121.7230, 121.6130, 122.1030, 121.7730, 122.3730, 121.8430, 121.9530,
            121.6130, 121.6130, 121.6130, 122.1030, 121.7730, 121.7730, 121.6130, 121.4230, 122.3730, 121.4230,
            121.6130
    };

    /**
     * Get all location labels for Region 4
     */
    public static String[] getRegion4Labels() {
        return REGION_4_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 4
     */
    public static double[] getRegion4Lats() {
        return REGION_4_LATS;
    }

    /**
     * Get all longitude coordinates for Region 4
     */
    public static double[] getRegion4Lons() {
        return REGION_4_LONS;
    }

    /**
     * Get location data for a specific index in Region 4
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion4Location(int index) {
        if (index >= 0 && index < REGION_4_LABELS.length) {
            return new LocationData(
                    REGION_4_LABELS[index],
                    REGION_4_LATS[index],
                    REGION_4_LONS[index]
            );
        }
        return null;
    }

    // Region 4B (MIMAROPA) - All cities and municipalities
    public static final String[] REGION_4B_LABELS = {
            // Occidental Mindoro
            "Mamburao, Occidental Mindoro",
            "Abra de Ilog, Occidental Mindoro",
            "Calintaan, Occidental Mindoro",
            "Looc, Occidental Mindoro",
            "Lubang, Occidental Mindoro",
            "Magarang, Occidental Mindoro",
            "Paluan, Occidental Mindoro",
            "Rizal, Occidental Mindoro",
            "Sablayan, Occidental Mindoro",
            "San Jose, Occidental Mindoro",
            "Santa Cruz, Occidental Mindoro",
            // Oriental Mindoro
            "Calapan, Oriental Mindoro",
            "Baco, Oriental Mindoro",
            "Bansud, Oriental Mindoro",
            "Bongabong, Oriental Mindoro",
            "Bulalacao, Oriental Mindoro",
            "Gloria, Oriental Mindoro",
            "Mansalay, Oriental Mindoro",
            "Naujan, Oriental Mindoro",
            "Pinamalayan, Oriental Mindoro",
            "Pola, Oriental Mindoro",
            "Puerto Galera, Oriental Mindoro",
            "Roxas, Oriental Mindoro",
            "San Teodoro, Oriental Mindoro",
            "Socorro, Oriental Mindoro",
            "Victoria, Oriental Mindoro",
            // Marinduque
            "Boac, Marinduque",
            "Buenavista, Marinduque",
            "Gasan, Marinduque",
            "Mogpog, Marinduque",
            "Santa Cruz, Marinduque",
            "Torrijos, Marinduque",
            // Romblon
            "Romblon, Romblon",
            "Alcantara, Romblon",
            "Banton, Romblon",
            "Cajidiocan, Romblon",
            "Calatrava, Romblon",
            "Concepcion, Romblon",
            "Corcuera, Romblon",
            "Ferrol, Romblon",
            "Looc, Romblon",
            "Magdiwang, Romblon",
            "Odiongan, Romblon",
            "San Agustin, Romblon",
            "San Andres, Romblon",
            "San Fernando, Romblon",
            "San Jose, Romblon",
            "Santa Fe, Romblon",
            "Santa Maria, Romblon",
            // Palawan
            "Puerto Princesa, Palawan",
            "Aborlan, Palawan",
            "Agutaya, Palawan",
            "Araceli, Palawan",
            "Balabac, Palawan",
            "Bataraza, Palawan",
            "Brooke's Point, Palawan",
            "Busuanga, Palawan",
            "Cagayancillo, Palawan",
            "Coron, Palawan",
            "Culion, Palawan",
            "Cuyo, Palawan",
            "Dumaran, Palawan",
            "El Nido, Palawan",
            "Kalayaan, Palawan",
            "Linapacan, Palawan",
            "Magsaysay, Palawan",
            "Narra, Palawan",
            "Quezon, Palawan",
            "Rizal, Palawan",
            "Roxas, Palawan",
            "San Vicente, Palawan",
            "Sofronio Española, Palawan",
            "Taytay, Palawan"
    };

    public static final double[] REGION_4B_LATS = {
            // Occidental Mindoro
            13.2236, 13.4436, 12.5736, 13.7236, 13.8536, 12.9636, 13.4236, 12.4736, 12.8336, 12.3536,
            13.0736,
            // Oriental Mindoro
            13.4125, 13.3575, 12.7895, 12.7295, 12.6195, 12.5095, 12.7595, 12.9495, 13.1895, 13.0595,
            13.1595, 13.4995, 12.5895, 13.3595, 13.0595, 13.1895,
            // Marinduque
            13.4446, 13.2586, 13.3186, 13.4746, 13.4796, 13.3146,
            // Romblon
            12.5757, 12.2657, 12.9257, 12.3657, 12.6207, 12.9257, 12.8057, 12.3357, 12.8557, 12.5857,
            12.4057, 12.5457, 12.5157, 12.3157, 12.5357, 12.1457, 12.5857,
            // Palawan
            9.7392, 9.4342, 11.1482, 10.4392, 8.0442, 8.5392, 8.7892, 12.0492, 9.5942, 12.0032,
            11.8942, 10.8482, 10.5292, 11.1942, 11.0472, 11.4732, 10.8942, 9.2682, 9.2392, 8.5062,
            10.3292, 10.5282, 8.4282, 10.8172
    };

    public static final double[] REGION_4B_LONS = {
            // Occidental Mindoro
            120.5956, 120.7256, 120.9256, 120.3456, 120.1256, 121.0856, 120.4656, 120.8656, 120.7756, 121.0456,
            121.4256,
            // Oriental Mindoro
            121.1803, 121.0983, 121.3583, 121.2583, 121.1083, 121.5083, 121.2583, 121.2083, 121.4583, 121.4783,
            121.4083, 121.4983, 121.1583, 121.2583, 121.0583, 121.2583,
            // Marinduque
            121.8416, 121.7756, 121.9016, 121.8616, 121.9866, 122.0866,
            // Romblon
            122.2707, 121.9707, 122.0707, 122.6407, 122.0707, 122.0707, 122.0507, 121.9707, 121.9707, 122.1307,
            121.9707, 122.1307, 122.1307, 121.9707, 121.9707, 122.0007, 122.0007,
            // Palawan
            118.7353, 118.5453, 120.9453, 119.9953, 117.2453, 117.6353, 117.7953, 119.9653, 121.0353, 120.2043,
            120.0243, 121.0353, 119.8653, 119.3853, 114.2953, 119.9053, 119.8253, 118.4053, 118.0353, 117.6353,
            119.3553, 119.5053, 117.4153, 119.4953
    };

    /**
     * Get all location labels for Region 4B
     */
    public static String[] getRegion4BLabels() {
        return REGION_4B_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 4B
     */
    public static double[] getRegion4BLats() {
        return REGION_4B_LATS;
    }

    /**
     * Get all longitude coordinates for Region 4B
     */
    public static double[] getRegion4BLons() {
        return REGION_4B_LONS;
    }

    /**
     * Get location data for a specific index in Region 4B
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion4BLocation(int index) {
        if (index >= 0 && index < REGION_4B_LABELS.length) {
            return new LocationData(
                    REGION_4B_LABELS[index],
                    REGION_4B_LATS[index],
                    REGION_4B_LONS[index]
            );
        }
        return null;
    }

    // Region 5 (Bicol Region) - All cities and municipalities
    public static final String[] REGION_5_LABELS = {
            // Albay
            "Legazpi, Albay",
            "Ligao, Albay",
            "Tabaco, Albay",
            "Bacacay, Albay",
            "Camalig, Albay",
            "Daraga, Albay",
            "Guinobatan, Albay",
            "Jovellar, Albay",
            "Libon, Albay",
            "Malilipot, Albay",
            "Malinao, Albay",
            "Manito, Albay",
            "Oas, Albay",
            "Pio Duran, Albay",
            "Polangui, Albay",
            "Rapu-Rapu, Albay",
            "Santo Domingo, Albay",
            "Tiwi, Albay",
            // Camarines Norte
            "Daet, Camarines Norte",
            "Basud, Camarines Norte",
            "Capalonga, Camarines Norte",
            "Jose Panganiban, Camarines Norte",
            "Labo, Camarines Norte",
            "Mercedes, Camarines Norte",
            "Paracale, Camarines Norte",
            "San Lorenzo Ruiz, Camarines Norte",
            "San Vicente, Camarines Norte",
            "Santa Elena, Camarines Norte",
            "Talisay, Camarines Norte",
            "Vinzons, Camarines Norte",
            // Camarines Sur
            "Naga, Camarines Sur",
            "Iriga, Camarines Sur",
            "Baao, Camarines Sur",
            "Balatan, Camarines Sur",
            "Bato, Camarines Sur",
            "Bombon, Camarines Sur",
            "Buhi, Camarines Sur",
            "Bula, Camarines Sur",
            "Cabusao, Camarines Sur",
            "Calabanga, Camarines Sur",
            "Camaligan, Camarines Sur",
            "Canaman, Camarines Sur",
            "Caramoan, Camarines Sur",
            "Del Gallego, Camarines Sur",
            "Gainza, Camarines Sur",
            "Garchitorena, Camarines Sur",
            "Goa, Camarines Sur",
            "Lagonoy, Camarines Sur",
            "Libmanan, Camarines Sur",
            "Lupi, Camarines Sur",
            "Magarao, Camarines Sur",
            "Milaor, Camarines Sur",
            "Minalabac, Camarines Sur",
            "Nabua, Camarines Sur",
            "Ocampo, Camarines Sur",
            "Pamplona, Camarines Sur",
            "Pasacao, Camarines Sur",
            "Pili, Camarines Sur",
            "Presentacion, Camarines Sur",
            "Ragay, Camarines Sur",
            "Sagñay, Camarines Sur",
            "San Fernando, Camarines Sur",
            "San Jose, Camarines Sur",
            "Sipocot, Camarines Sur",
            "Siruma, Camarines Sur",
            "Tigaon, Camarines Sur",
            "Tinambac, Camarines Sur",
            // Catanduanes
            "Virac, Catanduanes",
            "Bagamanoc, Catanduanes",
            "Baras, Catanduanes",
            "Bato, Catanduanes",
            "Caramoran, Catanduanes",
            "Gigmoto, Catanduanes",
            "Pandan, Catanduanes",
            "Panganiban, Catanduanes",
            "San Andres, Catanduanes",
            "San Miguel, Catanduanes",
            "Viga, Catanduanes",
            // Masbate
            "Masbate City, Masbate",
            "Aroroy, Masbate",
            "Baleno, Masbate",
            "Balud, Masbate",
            "Batuan, Masbate",
            "Cataingan, Masbate",
            "Cawayan, Masbate",
            "Claveria, Masbate",
            "Dimasalang, Masbate",
            "Esperanza, Masbate",
            "Mandaon, Masbate",
            "Milagros, Masbate",
            "Mobo, Masbate",
            "Monreal, Masbate",
            "Palanas, Masbate",
            "Pio V. Corpuz, Masbate",
            "Placer, Masbate",
            "San Fernando, Masbate",
            "San Jacinto, Masbate",
            "San Pascual, Masbate",
            "Uson, Masbate",
            // Sorsogon
            "Sorsogon City, Sorsogon",
            "Barcelona, Sorsogon",
            "Bulan, Sorsogon",
            "Bulusan, Sorsogon",
            "Casiguran, Sorsogon",
            "Castilla, Sorsogon",
            "Donsol, Sorsogon",
            "Gubat, Sorsogon",
            "Irosin, Sorsogon",
            "Juban, Sorsogon",
            "Magallanes, Sorsogon",
            "Matnog, Sorsogon",
            "Pilar, Sorsogon",
            "Prieto Diaz, Sorsogon",
            "Santa Magdalena, Sorsogon"
    };

    public static final double[] REGION_5_LATS = {
            // Albay
            13.1391, 13.2391, 13.3591, 13.2931, 13.1591, 13.1481, 13.1731, 13.0681, 13.2931, 13.3191,
            13.3991, 13.3191, 13.2591, 13.0331, 13.2931, 13.1861, 13.2391, 13.4591,
            // Camarines Norte
            14.1131, 14.0631, 14.3331, 14.2931, 14.1531, 14.1131, 14.2831, 14.0631, 14.1131, 14.1131,
            14.1131,
            // Camarines Sur
            13.6218, 13.4218, 13.4518, 13.3218, 13.3518, 13.4518, 13.4318, 13.4718, 13.7218, 13.7218,
            13.6218, 13.6218, 13.7718, 13.9218, 13.6218, 13.8818, 13.6918, 13.7418, 13.6918, 13.8218,
            13.6218, 13.6218, 13.6218, 13.4018, 13.5618, 13.5918, 13.5118, 13.5718, 13.7118, 13.8118,
            13.6018, 13.5918, 13.7018, 13.7718, 13.7718, 13.6418, 13.7918,
            // Catanduanes
            13.5798, 13.9398, 13.6598, 13.6098, 13.9398, 13.7798, 14.0398, 13.9998, 13.6198, 13.6998,
            13.8598,
            // Masbate
            12.3696, 12.5096, 12.4896, 12.0296, 12.4296, 12.0096, 12.0296, 12.8996, 12.1896, 11.5096,
            12.2296, 12.2296, 12.2996, 12.6496, 12.1396, 12.0096, 11.8696, 12.4896, 12.5696, 12.0696,
            12.2296,
            // Sorsogon
            12.9724, 12.8694, 12.6744, 12.7494, 12.8694, 12.9494, 12.9094, 12.9194, 12.6994, 12.8494,
            12.8294, 12.5894, 12.9194, 12.8594, 12.6194
    };

    public static final double[] REGION_5_LONS = {
            // Albay
            123.7347, 123.5447, 123.7147, 123.7947, 123.6547, 123.6947, 123.6047, 123.8047, 123.4047, 123.7047,
            123.7047, 123.8647, 123.4547, 123.4547, 123.4847, 124.1247, 123.7747, 123.6847,
            // Camarines Norte
            122.9541, 122.9541, 122.4941, 122.6941, 122.8341, 122.9541, 122.7841, 122.9541, 122.9541, 122.9541,
            122.9541,
            // Camarines Sur
            123.1940, 123.4140, 123.3440, 123.2340, 123.3440, 123.2540, 123.5140, 123.3140, 123.0940, 123.2140,
            123.3540, 123.1740, 123.8440, 122.9940, 123.1440, 123.3640, 123.4940, 123.5340, 123.0540, 123.5340,
            123.4040, 123.1740, 123.1740, 123.3740, 123.3640, 123.0940, 123.0440, 123.3040, 123.8840, 122.7940,
            123.9140, 123.2540, 123.5440, 122.9740, 123.2740, 123.5140, 123.3840,
            // Catanduanes
            124.2438, 124.2838, 124.1438, 124.3038, 124.1338, 124.3838, 124.1038, 124.4638, 124.0838, 124.3138,
            124.3438,
            // Masbate
            123.6296, 123.3996, 123.5096, 123.2296, 123.8096, 123.9996, 123.5896, 123.8596, 123.8596, 124.0396,
            123.3096, 123.5096, 123.3496, 123.6596, 123.9996, 124.0396, 123.9196, 123.9396, 123.7396, 123.6096,
            123.7796,
            // Sorsogon
            123.9939, 124.1439, 123.8739, 124.1339, 124.0139, 123.8839, 123.9139, 124.1239, 124.0339, 123.9839,
            124.1539, 124.0939, 123.8739, 124.1939, 124.1039
    };

    /**
     * Get all location labels for Region 5
     */
    public static String[] getRegion5Labels() {
        return REGION_5_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 5
     */
    public static double[] getRegion5Lats() {
        return REGION_5_LATS;
    }

    /**
     * Get all longitude coordinates for Region 5
     */
    public static double[] getRegion5Lons() {
        return REGION_5_LONS;
    }

    /**
     * Get location data for a specific index in Region 5
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion5Location(int index) {
        if (index >= 0 && index < REGION_5_LABELS.length) {
            return new LocationData(
                    REGION_5_LABELS[index],
                    REGION_5_LATS[index],
                    REGION_5_LONS[index]
            );
        }
        return null;
    }

    // Region 6 (Western Visayas) - All cities and municipalities
    public static final String[] REGION_6_LABELS = {
            // Aklan
            "Kalibo, Aklan",
            "Altavas, Aklan",
            "Balete, Aklan",
            "Banga, Aklan",
            "Batan, Aklan",
            "Buruanga, Aklan",
            "Ibajay, Aklan",
            "Lezo, Aklan",
            "Libacao, Aklan",
            "Madalag, Aklan",
            "Makato, Aklan",
            "Malay, Aklan",
            "Malinao, Aklan",
            "Nabas, Aklan",
            "New Washington, Aklan",
            "Numancia, Aklan",
            "Tangalan, Aklan",
            // Antique
            "San Jose, Antique",
            "Anini-y, Antique",
            "Barbaza, Antique",
            "Belison, Antique",
            "Bugasong, Antique",
            "Caluya, Antique",
            "Culasi, Antique",
            "Hamtic, Antique",
            "Laua-an, Antique",
            "Libertad, Antique",
            "Pandan, Antique",
            "Patnongon, Antique",
            "San Remigio, Antique",
            "Sebaste, Antique",
            "Sibalom, Antique",
            "Tibiao, Antique",
            "Tobias Fornier, Antique",
            "Valderrama, Antique",
            // Capiz
            "Roxas, Capiz",
            "Cuartero, Capiz",
            "Dao, Capiz",
            "Dumalag, Capiz",
            "Dumarao, Capiz",
            "Ivisan, Capiz",
            "Jamindan, Capiz",
            "Ma-ayon, Capiz",
            "Mambusao, Capiz",
            "Panay, Capiz",
            "Panitan, Capiz",
            "Pilar, Capiz",
            "Pontevedra, Capiz",
            "President Roxas, Capiz",
            "Sapi-an, Capiz",
            "Sigma, Capiz",
            "Tapaz, Capiz",
            // Guimaras
            "Jordan, Guimaras",
            "Buenavista, Guimaras",
            "Nueva Valencia, Guimaras",
            "San Lorenzo, Guimaras",
            "Sibunag, Guimaras",
            // Iloilo
            "Iloilo City, Iloilo",
            "Passi, Iloilo",
            "Ajuy, Iloilo",
            "Alimodian, Iloilo",
            "Anilao, Iloilo",
            "Badiangan, Iloilo",
            "Balasan, Iloilo",
            "Banate, Iloilo",
            "Barotac Nuevo, Iloilo",
            "Barotac Viejo, Iloilo",
            "Batad, Iloilo",
            "Bingawan, Iloilo",
            "Cabatuan, Iloilo",
            "Calinog, Iloilo",
            "Carles, Iloilo",
            "Concepcion, Iloilo",
            "Dingle, Iloilo",
            "Dueñas, Iloilo",
            "Dumangas, Iloilo",
            "Estancia, Iloilo",
            "Guimbal, Iloilo",
            "Igbaras, Iloilo",
            "Janiuay, Iloilo",
            "Lambunao, Iloilo",
            "Leganes, Iloilo",
            "Lemery, Iloilo",
            "Leon, Iloilo",
            "Maasin, Iloilo",
            "Miagao, Iloilo",
            "Mina, Iloilo",
            "New Lucena, Iloilo",
            "Oton, Iloilo",
            "Pavia, Iloilo",
            "Pototan, Iloilo",
            "San Dionisio, Iloilo",
            "San Enrique, Iloilo",
            "San Joaquin, Iloilo",
            "San Miguel, Iloilo",
            "San Rafael, Iloilo",
            "Santa Barbara, Iloilo",
            "Sara, Iloilo",
            "Tigbauan, Iloilo",
            "Tubungan, Iloilo",
            "Zarraga, Iloilo",
            // Negros Occidental
            "Bacolod, Negros Occidental",
            "Bago, Negros Occidental",
            "Cadiz, Negros Occidental",
            "Escalante, Negros Occidental",
            "Himamaylan, Negros Occidental",
            "Kabankalan, Negros Occidental",
            "La Carlota, Negros Occidental",
            "Sagay, Negros Occidental",
            "San Carlos, Negros Occidental",
            "Silay, Negros Occidental",
            "Sipalay, Negros Occidental",
            "Talisay, Negros Occidental",
            "Victorias, Negros Occidental",
            "Binalbagan, Negros Occidental",
            "Calatrava, Negros Occidental",
            "Candoni, Negros Occidental",
            "Cauayan, Negros Occidental",
            "Enrique B. Magalona, Negros Occidental",
            "Hinigaran, Negros Occidental",
            "Hinoba-an, Negros Occidental",
            "Ilog, Negros Occidental",
            "Isabela, Negros Occidental",
            "La Castellana, Negros Occidental",
            "Manapla, Negros Occidental",
            "Moises Padilla, Negros Occidental",
            "Murcia, Negros Occidental",
            "Pontevedra, Negros Occidental",
            "Pulupandan, Negros Occidental",
            "Salvador Benedicto, Negros Occidental",
            "San Enrique, Negros Occidental",
            "Toboso, Negros Occidental",
            "Valladolid, Negros Occidental"
    };

    public static final double[] REGION_6_LATS = {
            // Aklan
            11.7061, 11.5381, 11.5561, 11.6411, 11.7711, 11.8461, 11.8261, 11.6411, 11.4861, 11.5161,
            11.7161, 11.9611, 11.6411, 11.8261, 11.6411, 11.7061, 11.7761,
            // Antique
            10.7753, 10.4293, 11.2093, 10.8393, 11.0493, 11.7593, 11.4193, 10.6993, 11.1393, 10.8393,
            10.9493, 10.6993, 10.7793, 11.2993, 10.6193, 10.5193, 11.2493,
            // Capiz
            11.5853, 11.3413, 11.3913, 11.3413, 11.3413, 11.5213, 11.3413, 11.3913, 11.3413, 11.3413,
            11.4613, 11.3413, 11.3413, 11.3413, 11.3413, 11.3413, 11.2613,
            // Guimaras
            10.6013, 10.7013, 10.5013, 10.6413, 10.0313,
            // Iloilo
            10.7202, 11.1082, 11.1682, 10.8212, 10.9812, 11.0012, 11.4712, 11.0412, 10.8912, 11.0412,
            11.4212, 11.2312, 10.8812, 11.1212, 11.5712, 11.2012, 11.0012, 11.0012, 10.8412, 11.4512,
            10.6612, 10.7212, 11.0412, 10.7312, 10.7812, 11.2212, 10.7812, 10.8812, 10.6412, 11.0012,
            10.8812, 10.6912, 10.7812, 10.9412, 11.2012, 11.0012, 10.5912, 11.0012, 11.0012, 10.8212,
            11.0012, 10.7012, 10.6412, 10.8212,
            // Negros Occidental
            10.3157, 10.5387, 10.9487, 10.8387, 10.0987, 9.9887, 10.4187, 10.8987, 10.4887, 10.7987,
            9.7587, 10.7387, 10.8987, 10.1987, 10.5987, 9.8487, 9.9687, 10.8987, 10.1987, 9.6387,
            10.0287, 10.1987, 10.3587, 10.9587, 10.2687, 10.5987, 10.3687, 10.5187, 10.5987, 10.4087,
            10.7187, 10.4587
    };

    public static final double[] REGION_6_LONS = {
            // Aklan
            122.1119, 122.4919, 122.3619, 122.3019, 122.2719, 121.8919, 122.1619, 122.4919, 122.3019, 122.3019,
            122.2919, 121.9619, 122.3219, 122.0919, 122.4319, 122.4319, 122.2619,
            // Antique
            121.9513, 121.8313, 122.0513, 121.9513, 122.0513, 121.9713, 122.0513, 121.9513, 122.2513, 121.9513,
            122.0513, 121.9513, 122.0513, 122.0513, 122.0513, 121.9513, 122.0513,
            // Capiz
            122.7511, 122.6811, 122.6811, 122.6811, 122.6811, 122.6811, 122.6811, 122.6811, 122.6811, 122.6811,
            122.6811, 122.6811, 122.6811, 122.6811, 122.6811, 122.6811, 122.6811,
            // Guimaras
            122.5913, 122.6413, 122.5313, 122.6413, 121.9313,
            // Iloilo
            122.5621, 122.6321, 122.7821, 122.4321, 122.7421, 122.9421, 123.2221, 122.8121, 122.7021, 122.8121,
            123.2021, 122.8621, 122.7821, 122.5321, 123.1921, 123.0821, 122.6721, 122.6721, 122.7021, 123.1921,
            122.3321, 122.2621, 122.5321, 122.3821, 122.3821, 122.8421, 122.3821, 122.3821, 122.2321, 122.6721,
            122.7821, 122.5321, 122.3821, 122.6321, 123.0821, 122.6721, 122.2321, 122.6721, 122.6721, 122.3821,
            122.6721, 122.3821, 122.3821, 122.6321,
            // Negros Occidental
            123.8854, 122.8384, 123.3084, 123.4984, 122.8784, 122.8284, 123.0784, 123.4084, 123.4184, 122.9584,
            122.6184, 122.9584, 123.0784, 122.8584, 123.5184, 122.5984, 122.5984, 123.0784, 122.6984, 122.4484,
            122.6984, 122.9784, 123.0184, 123.1284, 123.0484, 123.1984, 122.8784, 122.7784, 123.1984, 122.6984,
            123.4684, 122.8284
    };

    /**
     * Get all location labels for Region 6
     */
    public static String[] getRegion6Labels() {
        return REGION_6_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 6
     */
    public static double[] getRegion6Lats() {
        return REGION_6_LATS;
    }

    /**
     * Get all longitude coordinates for Region 6
     */
    public static double[] getRegion6Lons() {
        return REGION_6_LONS;
    }

    /**
     * Get location data for a specific index in Region 6
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion6Location(int index) {
        if (index >= 0 && index < REGION_6_LABELS.length) {
            return new LocationData(
                    REGION_6_LABELS[index],
                    REGION_6_LATS[index],
                    REGION_6_LONS[index]
            );
        }
        return null;
    }

    // Region 7 (Central Visayas) - All cities and municipalities
    public static final String[] REGION_7_LABELS = {
            // Bohol
            "Tagbilaran, Bohol",
            "Alburquerque, Bohol",
            "Alicia, Bohol",
            "Anda, Bohol",
            "Antequera, Bohol",
            "Baclayon, Bohol",
            "Balilihan, Bohol",
            "Batuan, Bohol",
            "Bien Unido, Bohol",
            "Bilar, Bohol",
            "Buenavista, Bohol",
            "Calape, Bohol",
            "Candijay, Bohol",
            "Carmen, Bohol",
            "Catigbian, Bohol",
            "Clarin, Bohol",
            "Corella, Bohol",
            "Cortes, Bohol",
            "Dagohoy, Bohol",
            "Danao, Bohol",
            "Dauis, Bohol",
            "Dimiao, Bohol",
            "Duero, Bohol",
            "Garcia Hernandez, Bohol",
            "Getafe, Bohol",
            "Guindulman, Bohol",
            "Inabanga, Bohol",
            "Jagna, Bohol",
            "Lila, Bohol",
            "Loay, Bohol",
            "Loboc, Bohol",
            "Loon, Bohol",
            "Mabini, Bohol",
            "Maribojoc, Bohol",
            "Panglao, Bohol",
            "Pilar, Bohol",
            "President Carlos P. Garcia, Bohol",
            "Sagbayan, Bohol",
            "San Isidro, Bohol",
            "San Miguel, Bohol",
            "Sevilla, Bohol",
            "Sierra Bullones, Bohol",
            "Sikatuna, Bohol",
            "Talibon, Bohol",
            "Trinidad, Bohol",
            "Tubigon, Bohol",
            "Ubay, Bohol",
            "Valencia, Bohol",
            // Cebu
            "Cebu City, Cebu",
            "Lapu-Lapu, Cebu",
            "Mandaue, Cebu",
            "Talisay, Cebu",
            "Toledo, Cebu",
            "Bogo, Cebu",
            "Carcar, Cebu",
            "Danao, Cebu",
            "Naga, Cebu",
            "Alcantara, Cebu",
            "Alcoy, Cebu",
            "Alegria, Cebu",
            "Aloguinsan, Cebu",
            "Argao, Cebu",
            "Asturias, Cebu",
            "Badian, Cebu",
            "Balamban, Cebu",
            "Bantayan, Cebu",
            "Barili, Cebu",
            "Boljoon, Cebu",
            "Borbon, Cebu",
            "Carmen, Cebu",
            "Catmon, Cebu",
            "Compostela, Cebu",
            "Consolacion, Cebu",
            "Cordova, Cebu",
            "Daanbantayan, Cebu",
            "Dalaguete, Cebu",
            "Dumanjug, Cebu",
            "Ginatilan, Cebu",
            "Liloan, Cebu",
            "Madridejos, Cebu",
            "Malabuyoc, Cebu",
            "Medellin, Cebu",
            "Minglanilla, Cebu",
            "Moalboal, Cebu",
            "Oslob, Cebu",
            "Pilar, Cebu",
            "Pinamungajan, Cebu",
            "Poro, Cebu",
            "Ronda, Cebu",
            "Samboan, Cebu",
            "San Fernando, Cebu",
            "San Francisco, Cebu",
            "San Remigio, Cebu",
            "Santa Fe, Cebu",
            "Santander, Cebu",
            "Sibonga, Cebu",
            "Sogod, Cebu",
            "Tabogon, Cebu",
            "Tabuelan, Cebu",
            "Tuburan, Cebu",
            "Tudela, Cebu",
            // Negros Oriental
            "Dumaguete, Negros Oriental",
            "Bais, Negros Oriental",
            "Bayawan, Negros Oriental",
            "Canlaon, Negros Oriental",
            "Guihulngan, Negros Oriental",
            "Tanjay, Negros Oriental",
            "Amlan, Negros Oriental",
            "Ayungon, Negros Oriental",
            "Bacong, Negros Oriental",
            "Basay, Negros Oriental",
            "Bindoy, Negros Oriental",
            "Dauin, Negros Oriental",
            "Jimalalud, Negros Oriental",
            "La Libertad, Negros Oriental",
            "Mabinay, Negros Oriental",
            "Manjuyod, Negros Oriental",
            "Pamplona, Negros Oriental",
            "San Jose, Negros Oriental",
            "Santa Catalina, Negros Oriental",
            "Siaton, Negros Oriental",
            "Sibulan, Negros Oriental",
            "Tayasan, Negros Oriental",
            "Valencia, Negros Oriental",
            "Vallehermoso, Negros Oriental",
            "Zamboanguita, Negros Oriental",
            // Siquijor
            "Siquijor, Siquijor",
            "Enrique Villanueva, Siquijor",
            "Larena, Siquijor",
            "Lazi, Siquijor",
            "Maria, Siquijor",
            "San Juan, Siquijor"
    };

    public static final double[] REGION_7_LATS = {
            // Bohol
            9.6500, 9.6080, 9.9280, 9.7480, 9.7880, 9.6180, 9.7580, 9.7780, 10.1380, 9.7080,
            9.8780, 9.8880, 9.8180, 9.8180, 9.8580, 9.9680, 9.6880, 9.7280, 9.9180, 9.9280,
            9.6180, 9.6180, 9.7180, 9.6180, 10.1380, 9.7580, 10.0380, 9.6580, 9.6180, 9.6080,
            9.6380, 9.7980, 9.9280, 9.7480, 9.5780, 9.8280, 10.1380, 9.9180, 9.8780, 9.8080,
            9.6480, 9.8180, 9.6880, 10.1380, 10.0780, 9.9480, 10.0580, 9.6180,
            // Cebu
            10.3157, 10.3107, 10.3307, 10.2457, 10.3787, 11.0387, 10.1157, 10.5087, 10.2087, 9.9787,
            9.7087, 9.6187, 10.0787, 9.8787, 10.5687, 9.8687, 10.4487, 11.2087, 10.1387, 9.6187,
            10.8287, 10.5787, 10.4387, 10.4487, 10.3787, 10.2487, 11.2487, 9.9387, 9.6187, 9.6187,
            10.4087, 11.2787, 9.6187, 11.1287, 10.2487, 9.6187, 9.5187, 10.4087, 10.4487, 10.2487,
            10.4087, 9.6187, 10.2487, 10.4087, 11.2487, 11.2087, 9.3687, 9.6187, 9.6187, 10.2487,
            10.9487, 10.4087, 10.7387, 10.2487,
            // Negros Oriental
            9.3070, 9.5870, 9.3670, 10.3870, 10.2670, 9.5170, 9.4570, 9.8570, 9.2470, 9.4170,
            9.7570, 9.1870, 9.9870, 10.0870, 9.7270, 9.6970, 9.4870, 9.4170, 9.3270, 9.0670,
            9.3470, 9.9370, 9.5070, 10.3270, 9.1870,
            // Siquijor
            9.1870, 9.1870, 9.1870, 9.1270, 9.1870, 9.1870
    };

    public static final double[] REGION_7_LONS = {
            // Bohol
            123.8580, 123.9580, 124.4280, 124.5680, 123.8980, 123.8580, 123.9780, 124.1480, 124.3780, 124.0980,
            124.1480, 123.8980, 124.4980, 124.1980, 124.0280, 124.0280, 123.8780, 123.8980, 124.4280, 124.0280,
            123.8580, 124.1980, 124.4280, 124.1980, 124.4980, 124.3480, 124.0280, 124.3680, 124.0980, 124.0180,
            124.0380, 123.8980, 124.5280, 123.8580, 123.7780, 124.3280, 124.3680, 124.0280, 124.4280, 124.3480,
            124.0980, 124.2880, 123.8980, 124.3480, 124.3480, 123.8980, 124.4680, 124.1980,
            // Cebu
            123.8854, 123.9454, 123.9354, 123.8454, 123.6454, 124.0054, 123.6354, 123.9354, 123.7754, 123.4054,
            123.5154, 123.4054, 123.5454, 123.5154, 123.7854, 123.3754, 123.7954, 123.7854, 123.5154, 123.3754,
            123.9554, 123.9554, 123.9554, 123.7954, 123.9554, 123.9454, 124.0054, 123.5154, 123.3754, 123.3754,
            123.9554, 124.1154, 123.3754, 123.9554, 123.9554, 123.3754, 123.3754, 123.9554, 123.9554, 123.9554,
            123.9554, 123.3754, 123.9554, 123.9554, 123.9554, 123.7854, 123.3754, 123.3754, 123.3754, 123.9554,
            124.0054, 123.9554, 123.5554, 123.9554,
            // Negros Oriental
            123.9180, 123.0980, 122.7980, 123.2080, 123.2780, 123.1580, 123.1880, 123.1480, 123.2880, 123.1680,
            123.0980, 123.2580, 123.1980, 123.2180, 123.0180, 123.0980, 123.0880, 123.0380, 122.9580, 122.9980,
            123.2880, 123.0180, 123.0180, 123.2480, 123.2580,
            // Siquijor
            123.5180, 123.5180, 123.5180, 123.5180, 123.5180, 123.5180
    };

    /**
     * Get all location labels for Region 7
     */
    public static String[] getRegion7Labels() {
        return REGION_7_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 7
     */
    public static double[] getRegion7Lats() {
        return REGION_7_LATS;
    }

    /**
     * Get all longitude coordinates for Region 7
     */
    public static double[] getRegion7Lons() {
        return REGION_7_LONS;
    }

    /**
     * Get location data for a specific index in Region 7
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion7Location(int index) {
        if (index >= 0 && index < REGION_7_LABELS.length) {
            return new LocationData(
                    REGION_7_LABELS[index],
                    REGION_7_LATS[index],
                    REGION_7_LONS[index]
            );
        }
        return null;
    }

    // Region 8 (Eastern Visayas) - All cities and municipalities
    public static final String[] REGION_8_LABELS = {
            // Biliran
            "Naval, Biliran",
            "Almeria, Biliran",
            "Biliran, Biliran",
            "Cabucgayan, Biliran",
            "Caibiran, Biliran",
            "Culaba, Biliran",
            "Kawayan, Biliran",
            "Maripipi, Biliran",
            // Eastern Samar
            "Borongan, Eastern Samar",
            "Arteche, Eastern Samar",
            "Balangiga, Eastern Samar",
            "Balangkayan, Eastern Samar",
            "Can-avid, Eastern Samar",
            "Dolores, Eastern Samar",
            "General MacArthur, Eastern Samar",
            "Giporlos, Eastern Samar",
            "Guiuan, Eastern Samar",
            "Hernani, Eastern Samar",
            "Jipapad, Eastern Samar",
            "Lawaan, Eastern Samar",
            "Llorente, Eastern Samar",
            "Maslog, Eastern Samar",
            "Maydolong, Eastern Samar",
            "Mercedes, Eastern Samar",
            "Oras, Eastern Samar",
            "Quinapondan, Eastern Samar",
            "Salcedo, Eastern Samar",
            "San Julian, Eastern Samar",
            "San Policarpo, Eastern Samar",
            "Sulat, Eastern Samar",
            "Taft, Eastern Samar",
            // Leyte
            "Tacloban, Leyte",
            "Ormoc, Leyte",
            "Baybay, Leyte",
            "Abuyog, Leyte",
            "Alangalang, Leyte",
            "Albuera, Leyte",
            "Babatngon, Leyte",
            "Barugo, Leyte",
            "Bato, Leyte",
            "Burauen, Leyte",
            "Calubian, Leyte",
            "Capoocan, Leyte",
            "Carigara, Leyte",
            "Dagami, Leyte",
            "Dulag, Leyte",
            "Hilongos, Leyte",
            "Hindang, Leyte",
            "Inopacan, Leyte",
            "Isabel, Leyte",
            "Jaro, Leyte",
            "Javier, Leyte",
            "Julita, Leyte",
            "Kananga, Leyte",
            "La Paz, Leyte",
            "Leyte, Leyte",
            "MacArthur, Leyte",
            "Mahaplag, Leyte",
            "Matag-ob, Leyte",
            "Matalom, Leyte",
            "Mayorga, Leyte",
            "Merida, Leyte",
            "Palo, Leyte",
            "Palompon, Leyte",
            "Pastrana, Leyte",
            "San Isidro, Leyte",
            "San Miguel, Leyte",
            "Santa Fe, Leyte",
            "Tabango, Leyte",
            "Tabontabon, Leyte",
            "Tanauan, Leyte",
            "Tolosa, Leyte",
            "Tunga, Leyte",
            "Villaba, Leyte",
            // Northern Samar
            "Catarman, Northern Samar",
            "Allen, Northern Samar",
            "Biri, Northern Samar",
            "Bobon, Northern Samar",
            "Capul, Northern Samar",
            "Catubig, Northern Samar",
            "Gamay, Northern Samar",
            "Laoang, Northern Samar",
            "Lapinig, Northern Samar",
            "Las Navas, Northern Samar",
            "Lavezares, Northern Samar",
            "Lope de Vega, Northern Samar",
            "Mapanas, Northern Samar",
            "Mondragon, Northern Samar",
            "Palapag, Northern Samar",
            "Pambujan, Northern Samar",
            "Rosario, Northern Samar",
            "San Antonio, Northern Samar",
            "San Isidro, Northern Samar",
            "San Jose, Northern Samar",
            "San Roque, Northern Samar",
            "San Vicente, Northern Samar",
            "Silvino Lobos, Northern Samar",
            "Victoria, Northern Samar",
            // Samar
            "Catbalogan, Samar",
            "Calbayog, Samar",
            "Almagro, Samar",
            "Basey, Samar",
            "Calbiga, Samar",
            "Daram, Samar",
            "Gandara, Samar",
            "Hinabangan, Samar",
            "Jiabong, Samar",
            "Marabut, Samar",
            "Matuguinao, Samar",
            "Motiong, Samar",
            "Pagsanghan, Samar",
            "Paranas, Samar",
            "Pinabacdao, Samar",
            "San Jorge, Samar",
            "San Jose de Buan, Samar",
            "San Sebastian, Samar",
            "Santa Rita, Samar",
            "Santo Niño, Samar",
            "Tagapul-an, Samar",
            "Talalora, Samar",
            "Tarangnan, Samar",
            "Villareal, Samar",
            "Zumarraga, Samar",
            // Southern Leyte
            "Maasin, Southern Leyte",
            "Anahawan, Southern Leyte",
            "Bontoc, Southern Leyte",
            "Hinunangan, Southern Leyte",
            "Hinundayan, Southern Leyte",
            "Libagon, Southern Leyte",
            "Liloan, Southern Leyte",
            "Limasawa, Southern Leyte",
            "Macrohon, Southern Leyte",
            "Malitbog, Southern Leyte",
            "Padre Burgos, Southern Leyte",
            "Pintuyan, Southern Leyte",
            "Saint Bernard, Southern Leyte",
            "San Francisco, Southern Leyte",
            "San Juan, Southern Leyte",
            "San Ricardo, Southern Leyte",
            "Silago, Southern Leyte",
            "Sogod, Southern Leyte",
            "Tomas Oppus, Southern Leyte"
    };

    public static final double[] REGION_8_LATS = {
            // Biliran
            11.5638, 11.6238, 11.4838, 11.4838, 11.5638, 11.4838, 11.6838, 11.7838,
            // Eastern Samar
            11.6458, 12.2658, 11.1258, 11.4658, 12.0258, 12.0658, 11.2458, 11.1258,
            11.0358, 11.3258, 12.2858, 11.4158, 12.2858, 11.5058, 11.1058, 12.1458,
            11.5858, 11.3258, 11.7458, 12.0058, 11.3258, 11.9058,
            // Leyte
            11.2410, 11.0410, 10.6880, 10.7480, 11.2380, 10.9180, 11.4180, 11.2380,
            10.3280, 10.8780, 11.2380, 11.2380, 11.3580, 11.0580, 10.9580, 10.3780,
            10.3780, 10.4980, 10.9180, 11.1780, 10.7980, 10.8780, 11.1780, 11.1780,
            11.1780, 11.1780, 10.6080, 11.1480, 10.2880, 10.8780, 11.1780, 11.1780,
            11.0480, 11.1780, 11.1780, 11.1780, 11.1780, 11.2380, 11.0980, 11.1780,
            11.1780, 11.1780, 11.1780, 11.2380,
            // Northern Samar
            12.4988, 12.4988, 12.6788, 12.5288, 12.4288, 12.4088, 12.3988, 12.5688,
            12.3088, 12.3488, 12.5688, 12.2988, 12.4988, 12.4988, 12.5488, 12.5688,
            12.4988, 12.4188, 12.4988, 12.4988, 12.4988, 12.4988, 12.2988, 12.4488,
            // Samar
            11.7753, 12.0668, 11.9153, 11.2753, 11.6253, 11.6353, 12.0153, 11.7453,
            11.8553, 11.1253, 12.1153, 11.7753, 11.7753, 11.7753, 11.6253, 11.7753,
            12.0553, 11.7753, 11.4453, 11.8553, 11.9353, 11.7753, 11.7753, 11.7753,
            11.9353,
            // Southern Leyte
            10.1288, 10.2888, 10.3488, 10.3788, 10.3488, 10.2988, 10.1588, 9.8988,
            10.1288, 10.5688, 10.0288, 9.9488, 10.2888, 10.6488, 10.1288, 10.1288,
            10.2888, 10.3788, 10.1288
    };

    public static final double[] REGION_8_LONS = {
            // Biliran
            124.4718, 124.3818, 124.4818, 124.5518, 124.5018, 124.5418, 124.4018, 124.1718,
            // Eastern Samar
            125.4378, 125.3478, 125.5178, 125.5178, 125.4978, 125.6578, 125.5378, 125.4578,
            125.7278, 125.6178, 125.4378, 125.4878, 125.4378, 125.4978, 125.5178, 125.4378,
            125.7278, 125.6178, 125.4378, 125.4678, 125.6178, 125.4178,
            // Leyte
            124.8093, 124.6075, 124.8093, 125.0113, 124.8093, 124.6893, 124.8093, 124.7293,
            124.8093, 124.8093, 124.8093, 124.8093, 124.8093, 124.8093, 125.0313, 124.7493,
            124.8093, 124.8093, 124.8093, 124.8093, 124.8093, 124.8093, 124.8093, 124.8093,
            124.8093, 124.8093, 124.8093, 124.8093, 124.8093, 124.8093, 124.8093, 124.8093,
            124.8093, 124.8093, 124.8093, 124.8093, 124.8093, 124.8093, 124.8093, 124.8093,
            124.8093, 124.8093, 124.8093, 124.8093,
            // Northern Samar
            124.6518, 124.2818, 124.3818, 124.5718, 124.1818, 125.0518, 125.0518, 124.5618,
            125.3018, 125.0818, 124.6518, 124.6518, 125.0518, 124.7518, 125.0818, 124.6518,
            124.6518, 124.6518, 124.6518, 124.6518, 124.6518, 124.6518, 125.3018, 124.6518,
            // Samar
            124.8865, 124.5977, 124.7665, 125.0665, 125.0265, 124.7965, 124.8465, 125.1665,
            124.8865, 125.1465, 124.8665, 124.8865, 124.8865, 124.8865, 124.8865, 124.8865,
            125.0065, 124.8865, 124.8865, 124.8865, 124.8865, 124.8865, 124.8865, 124.8865,
            124.8865,
            // Southern Leyte
            125.0018, 125.0018, 124.7018, 125.2018, 125.2018, 125.0018, 125.0518, 125.1518,
            125.0018, 125.0018, 125.0018, 125.2518, 125.2018, 125.0518, 125.0018, 125.2518,
            125.2518, 124.9518, 125.0018
    };

    /**
     * Get all location labels for Region 8
     */
    public static String[] getRegion8Labels() {
        return REGION_8_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 8
     */
    public static double[] getRegion8Lats() {
        return REGION_8_LATS;
    }

    /**
     * Get all longitude coordinates for Region 8
     */
    public static double[] getRegion8Lons() {
        return REGION_8_LONS;
    }

    /**
     * Get location data for a specific index in Region 8
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion8Location(int index) {
        if (index >= 0 && index < REGION_8_LABELS.length) {
            return new LocationData(
                    REGION_8_LABELS[index],
                    REGION_8_LATS[index],
                    REGION_8_LONS[index]
            );
        }
        return null;
    }

    // Region 9 (Zamboanga Peninsula) - All cities and municipalities
    public static final String[] REGION_9_LABELS = {
            // Zamboanga del Norte
            "Dipolog, Zamboanga del Norte",
            "Dapitan, Zamboanga del Norte",
            "Bacungan, Zamboanga del Norte",
            "Baliguian, Zamboanga del Norte",
            "Godod, Zamboanga del Norte",
            "Gutalac, Zamboanga del Norte",
            "Jose Dalman, Zamboanga del Norte",
            "Kalawit, Zamboanga del Norte",
            "Katipunan, Zamboanga del Norte",
            "La Libertad, Zamboanga del Norte",
            "Labason, Zamboanga del Norte",
            "Liloy, Zamboanga del Norte",
            "Manukan, Zamboanga del Norte",
            "Mutia, Zamboanga del Norte",
            "Piñan, Zamboanga del Norte",
            "Polanco, Zamboanga del Norte",
            "President Manuel A. Roxas, Zamboanga del Norte",
            "Rizal, Zamboanga del Norte",
            "Salug, Zamboanga del Norte",
            "Sergio Osmeña Sr., Zamboanga del Norte",
            "Siayan, Zamboanga del Norte",
            "Sibuco, Zamboanga del Norte",
            "Sibutad, Zamboanga del Norte",
            "Sindangan, Zamboanga del Norte",
            "Siocon, Zamboanga del Norte",
            "Sirawai, Zamboanga del Norte",
            "Tampilisan, Zamboanga del Norte",
            // Zamboanga del Sur
            "Pagadian, Zamboanga del Sur",
            "Zamboanga City, Zamboanga del Sur",
            "Aurora, Zamboanga del Sur",
            "Bayog, Zamboanga del Sur",
            "Dimataling, Zamboanga del Sur",
            "Dinas, Zamboanga del Sur",
            "Dumalinao, Zamboanga del Sur",
            "Dumingag, Zamboanga del Sur",
            "Guipos, Zamboanga del Sur",
            "Josefina, Zamboanga del Sur",
            "Kumalarang, Zamboanga del Sur",
            "Labangan, Zamboanga del Sur",
            "Lakewood, Zamboanga del Sur",
            "Lapuyan, Zamboanga del Sur",
            "Mahayag, Zamboanga del Sur",
            "Margosatubig, Zamboanga del Sur",
            "Midsalip, Zamboanga del Sur",
            "Molave, Zamboanga del Sur",
            "Pitogo, Zamboanga del Sur",
            "Ramon Magsaysay, Zamboanga del Sur",
            "San Miguel, Zamboanga del Sur",
            "San Pablo, Zamboanga del Sur",
            "Sominot, Zamboanga del Sur",
            "Tabina, Zamboanga del Sur",
            "Tambulig, Zamboanga del Sur",
            "Tigbao, Zamboanga del Sur",
            "Tukuran, Zamboanga del Sur",
            "Vincenzo A. Sagun, Zamboanga del Sur",
            // Zamboanga Sibugay
            "Ipil, Zamboanga Sibugay",
            "Alicia, Zamboanga Sibugay",
            "Buug, Zamboanga Sibugay",
            "Diplahan, Zamboanga Sibugay",
            "Imelda, Zamboanga Sibugay",
            "Kabasalan, Zamboanga Sibugay",
            "Mabuhay, Zamboanga Sibugay",
            "Malangas, Zamboanga Sibugay",
            "Naga, Zamboanga Sibugay",
            "Olutanga, Zamboanga Sibugay",
            "Payao, Zamboanga Sibugay",
            "Roseller Lim, Zamboanga Sibugay",
            "Siay, Zamboanga Sibugay",
            "Talusan, Zamboanga Sibugay",
            "Titay, Zamboanga Sibugay",
            "Tungawan, Zamboanga Sibugay"
    };

    public static final double[] REGION_9_LATS = {
            // Zamboanga del Norte
            8.5885, 8.6555, 8.5885, 8.0085, 8.0085, 8.0085, 8.5885, 8.0085, 8.5085, 8.5085,
            8.0085, 8.1085, 8.5085, 8.4285, 8.5085, 8.5085, 8.5085, 8.5085, 8.5885, 8.5085,
            8.0085, 8.0085, 8.5885, 8.2085, 8.0085, 8.0085, 8.0085,
            // Zamboanga del Sur
            7.8257, 6.9214, 7.9487, 7.8287, 7.5287, 7.6287, 7.8287, 8.1687, 7.7287, 8.2087,
            7.7487, 7.8687, 7.9487, 7.8287, 8.0087, 7.8287, 8.0087, 7.6287, 7.6287, 7.6287,
            7.6287, 7.6287, 7.6287, 7.6287, 7.6287, 7.6287, 7.6287, 7.6287, 7.6287, 7.6287,
            7.6287,
            // Zamboanga Sibugay
            7.7847, 7.5047, 7.7247, 7.8047, 7.6447, 7.7947, 7.4247, 7.6247, 7.7747, 7.3247,
            7.5847, 7.6547, 7.7047, 7.4147, 7.8047, 7.5047
    };

    public static final double[] REGION_9_LONS = {
            // Zamboanga del Norte
            123.3406, 123.4106, 123.3406, 122.3406, 122.3406, 122.3406, 123.3406, 122.3406, 123.3406, 123.3406,
            122.3406, 122.3406, 123.3406, 123.3406, 123.3406, 123.3406, 123.3406, 123.3406, 123.3406, 123.3406,
            122.3406, 122.3406, 123.3406, 122.3406, 122.3406, 122.3406, 122.3406,
            // Zamboanga del Sur
            123.4385, 122.0790, 123.4385, 123.3385, 123.3385, 123.3385, 123.4385, 123.3385, 123.3385, 123.3385,
            123.3385, 123.4385, 123.3385, 123.3385, 123.3385, 123.3385, 123.3385, 123.3385, 123.3385, 123.3385,
            123.3385, 123.3385, 123.3385, 123.3385, 123.3385, 123.3385, 123.3385, 123.3385, 123.3385, 123.3385,
            123.3385,
            // Zamboanga Sibugay
            122.5847, 122.5847, 122.5847, 122.5847, 122.5847, 122.5847, 122.5847, 122.5847, 122.5847, 122.5847,
            122.5847, 122.5847, 122.5847, 122.5847, 122.5847, 122.5847
    };

    /**
     * Get all location labels for Region 9
     */
    public static String[] getRegion9Labels() {
        return REGION_9_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 9
     */
    public static double[] getRegion9Lats() {
        return REGION_9_LATS;
    }

    /**
     * Get all longitude coordinates for Region 9
     */
    public static double[] getRegion9Lons() {
        return REGION_9_LONS;
    }

    /**
     * Get location data for a specific index in Region 9
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion9Location(int index) {
        if (index >= 0 && index < REGION_9_LABELS.length) {
            return new LocationData(
                    REGION_9_LABELS[index],
                    REGION_9_LATS[index],
                    REGION_9_LONS[index]
            );
        }
        return null;
    }

    // Region 10 (Northern Mindanao) - All cities and municipalities
    public static final String[] REGION_10_LABELS = {
            // Bukidnon
            "Malaybalay, Bukidnon",
            "Valencia, Bukidnon",
            "Baungon, Bukidnon",
            "Cabanglasan, Bukidnon",
            "Damulog, Bukidnon",
            "Dangcagan, Bukidnon",
            "Don Carlos, Bukidnon",
            "Impasugong, Bukidnon",
            "Kadingilan, Bukidnon",
            "Kalilangan, Bukidnon",
            "Kibawe, Bukidnon",
            "Kitaotao, Bukidnon",
            "Lantapan, Bukidnon",
            "Libona, Bukidnon",
            "Malitbog, Bukidnon",
            "Manolo Fortich, Bukidnon",
            "Maramag, Bukidnon",
            "Pangantucan, Bukidnon",
            "Quezon, Bukidnon",
            "San Fernando, Bukidnon",
            "Sumilao, Bukidnon",
            "Talakag, Bukidnon",
            // Camiguin
            "Mambajao, Camiguin",
            "Catarman, Camiguin",
            "Guinsiliban, Camiguin",
            "Mahinog, Camiguin",
            "Sagay, Camiguin",
            // Lanao del Norte
            "Tubod, Lanao del Norte",
            "Iligan, Lanao del Norte",
            "Bacolod, Lanao del Norte",
            "Baloi, Lanao del Norte",
            "Baroy, Lanao del Norte",
            "Kapatagan, Lanao del Norte",
            "Kauswagan, Lanao del Norte",
            "Kolambugan, Lanao del Norte",
            "Lala, Lanao del Norte",
            "Linamon, Lanao del Norte",
            "Magsaysay, Lanao del Norte",
            "Maigo, Lanao del Norte",
            "Matungao, Lanao del Norte",
            "Munai, Lanao del Norte",
            "Nunungan, Lanao del Norte",
            "Pantao Ragat, Lanao del Norte",
            "Pantar, Lanao del Norte",
            "Poona Piagapo, Lanao del Norte",
            "Salvador, Lanao del Norte",
            "Sapad, Lanao del Norte",
            "Sultan Naga Dimaporo, Lanao del Norte",
            "Tagoloan, Lanao del Norte",
            "Tangcal, Lanao del Norte",
            "Tubod, Lanao del Norte",
            // Misamis Occidental
            "Oroquieta, Misamis Occidental",
            "Ozamiz, Misamis Occidental",
            "Tangub, Misamis Occidental",
            "Aloran, Misamis Occidental",
            "Baliangao, Misamis Occidental",
            "Bonifacio, Misamis Occidental",
            "Calamba, Misamis Occidental",
            "Clarin, Misamis Occidental",
            "Concepcion, Misamis Occidental",
            "Don Victoriano Chiongbian, Misamis Occidental",
            "Jimenez, Misamis Occidental",
            "Lopez Jaena, Misamis Occidental",
            "Panaon, Misamis Occidental",
            "Plaridel, Misamis Occidental",
            "Sapang Dalaga, Misamis Occidental",
            "Sinacaban, Misamis Occidental",
            "Tudela, Misamis Occidental",
            // Misamis Oriental
            "Cagayan de Oro, Misamis Oriental",
            "Gingoog, Misamis Oriental",
            "Alubijid, Misamis Oriental",
            "Balingasag, Misamis Oriental",
            "Balingoan, Misamis Oriental",
            "Binuangan, Misamis Oriental",
            "Claveria, Misamis Oriental",
            "El Salvador, Misamis Oriental",
            "Gitagum, Misamis Oriental",
            "Initao, Misamis Oriental",
            "Jasaan, Misamis Oriental",
            "Kinoguitan, Misamis Oriental",
            "Lagonglong, Misamis Oriental",
            "Laguindingan, Misamis Oriental",
            "Libertad, Misamis Oriental",
            "Lugait, Misamis Oriental",
            "Magsaysay, Misamis Oriental",
            "Manticao, Misamis Oriental",
            "Medina, Misamis Oriental",
            "Naawan, Misamis Oriental",
            "Opol, Misamis Oriental",
            "Salay, Misamis Oriental",
            "Sugbongcogon, Misamis Oriental",
            "Tagoloan, Misamis Oriental",
            "Talisayan, Misamis Oriental",
            "Villanueva, Misamis Oriental"
    };

    public static final double[] REGION_10_LATS = {
            // Bukidnon
            8.1579, 7.9079, 8.3079, 8.1179, 7.4879, 7.6179, 7.6879, 8.3379, 7.6179, 7.6179,
            7.7179, 7.4879, 8.0079, 8.3379, 8.5779, 8.3379, 7.7579, 7.8379, 7.6879, 7.9079,
            8.3079, 8.2179,
            // Camiguin
            9.2500, 9.2000, 9.1000, 9.1500, 9.1000,
            // Lanao del Norte
            8.0555, 8.2333, 8.0555, 8.0555, 8.0555, 7.9055, 8.1555, 8.0555, 8.0555, 8.0555,
            8.0555, 8.0555, 8.0555, 8.0555, 8.0555, 8.0555, 8.0555, 8.0555, 8.0555, 8.0555,
            8.0555, 8.0555, 8.0555, 8.0555, 8.0555,
            // Misamis Occidental
            8.4855, 8.1455, 8.0555, 8.4855, 8.4855, 8.4855, 8.4855, 8.4855, 8.4855, 8.4855,
            8.4855, 8.4855, 8.4855, 8.4855, 8.4855, 8.4855, 8.4855,
            // Misamis Oriental
            8.4542, 8.8742, 8.5742, 8.7442, 9.0042, 8.9142, 8.6142, 8.5542, 8.5542, 8.4942,
            8.6542, 8.8442, 8.8042, 8.5542, 8.5542, 8.3342, 8.5542, 8.4042, 8.9142, 8.4342,
            8.5542, 8.8742, 8.9542, 8.5542, 8.9942, 8.5842
    };

    public static final double[] REGION_10_LONS = {
            // Bukidnon
            125.0919, 125.0919, 124.8519, 125.3219, 124.9519, 125.0019, 125.0019, 125.0019, 124.9519, 124.8519,
            124.9519, 125.0019, 125.0019, 125.0019, 125.0019, 125.0019, 125.0019, 124.8519, 125.0019, 125.2519,
            124.8519, 124.8519,
            // Camiguin
            124.7167, 124.6667, 124.7167, 124.7667, 124.7167,
            // Lanao del Norte
            123.7919, 124.2455, 123.7919, 124.2419, 123.7919, 123.7919, 124.2419, 123.7919, 123.7919, 124.2419,
            123.7919, 124.2419, 123.7919, 123.7919, 123.7919, 124.2419, 124.2419, 124.2419, 124.2419, 124.2419,
            124.2419, 124.2419, 124.2419, 124.2419, 123.7919,
            // Misamis Occidental
            123.7919, 123.8419, 123.7919, 123.7919, 123.7919, 123.7919, 123.7919, 123.7919, 123.7919, 123.7919,
            123.7919, 123.7919, 123.7919, 123.7919, 123.7919, 123.7919, 123.7919,
            // Misamis Oriental
            124.6439, 125.1039, 124.5739, 124.7939, 125.0039, 124.6439, 124.9039, 124.5039, 124.4039, 124.3039,
            124.7639, 124.8039, 124.7939, 124.5739, 124.4039, 124.2439, 124.6439, 124.3539, 125.0239, 124.4439,
            124.5739, 124.9539, 125.0239, 124.6739, 124.9439, 124.7739
    };

    /**
     * Get all location labels for Region 10
     */
    public static String[] getRegion10Labels() {
        return REGION_10_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 10
     */
    public static double[] getRegion10Lats() {
        return REGION_10_LATS;
    }

    /**
     * Get all longitude coordinates for Region 10
     */
    public static double[] getRegion10Lons() {
        return REGION_10_LONS;
    }

    /**
     * Get location data for a specific index in Region 10
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion10Location(int index) {
        if (index >= 0 && index < REGION_10_LABELS.length) {
            return new LocationData(
                    REGION_10_LABELS[index],
                    REGION_10_LATS[index],
                    REGION_10_LONS[index]
            );
        }
        return null;
    }

    // Region 11 (Davao Region) - All cities and municipalities
    public static final String[] REGION_11_LABELS = {
            // Davao de Oro
            "Nabunturan, Davao de Oro",
            "Compostela, Davao de Oro",
            "Laak, Davao de Oro",
            "Mabini, Davao de Oro",
            "Maco, Davao de Oro",
            "Maragusan, Davao de Oro",
            "Mawab, Davao de Oro",
            "Monkayo, Davao de Oro",
            "Montevista, Davao de Oro",
            "New Bataan, Davao de Oro",
            "Pantukan, Davao de Oro",
            // Davao del Norte
            "Tagum, Davao del Norte",
            "Panabo, Davao del Norte",
            "Island Garden City of Samal, Davao del Norte",
            "Asuncion, Davao del Norte",
            "Braulio E. Dujali, Davao del Norte",
            "Carmen, Davao del Norte",
            "Kapalong, Davao del Norte",
            "New Corella, Davao del Norte",
            "San Isidro, Davao del Norte",
            "Santo Tomas, Davao del Norte",
            "Talaingod, Davao del Norte",
            // Davao del Sur
            "Digos, Davao del Sur",
            "Davao City, Davao del Sur",
            "Bansalan, Davao del Sur",
            "Don Marcelino, Davao del Sur",
            "Hagonoy, Davao del Sur",
            "Jose Abad Santos, Davao del Sur",
            "Kiblawan, Davao del Sur",
            "Magsaysay, Davao del Sur",
            "Malalag, Davao del Sur",
            "Malita, Davao del Sur",
            "Matanao, Davao del Sur",
            "Padada, Davao del Sur",
            "Santa Cruz, Davao del Sur",
            "Santa Maria, Davao del Sur",
            "Sarangani, Davao del Sur",
            "Sulop, Davao del Sur",
            // Davao Occidental
            "Malita, Davao Occidental",
            "Don Marcelino, Davao Occidental",
            "Jose Abad Santos, Davao Occidental",
            "Santa Maria, Davao Occidental",
            "Sarangani, Davao Occidental",
            // Davao Oriental
            "Mati, Davao Oriental",
            "Baganga, Davao Oriental",
            "Banaybanay, Davao Oriental",
            "Boston, Davao Oriental",
            "Caraga, Davao Oriental",
            "Cateel, Davao Oriental",
            "Governor Generoso, Davao Oriental",
            "Lupon, Davao Oriental",
            "Manay, Davao Oriental",
            "San Isidro, Davao Oriental",
            "Tarragona, Davao Oriental"
    };

    public static final double[] REGION_11_LATS = {
            // Davao de Oro
            7.6079, 7.6079, 7.8579, 7.3079, 7.6079, 7.3079, 7.6079, 7.6079, 7.6079, 7.6079,
            7.6079,
            // Davao del Norte
            7.4479, 7.3079, 7.1079, 7.6079, 7.4479, 7.6079, 7.6079, 7.6079, 7.6079, 7.6079,
            7.6079,
            // Davao del Sur
            6.7579, 7.0736, 6.7579, 6.3079, 6.7579, 6.0079, 6.6079, 6.7579, 6.6079, 6.4079,
            6.7579, 6.7579, 6.7579, 6.7579, 6.5079, 6.0079, 6.7579,
            // Davao Occidental
            6.4079, 6.3079, 6.0079, 6.5079, 6.0079,
            // Davao Oriental
            6.9079, 7.5079, 7.1079, 7.5079, 7.3079, 7.5079, 6.6079, 6.9079, 7.2079, 6.9079,
            7.4079
    };

    public static final double[] REGION_11_LONS = {
            // Davao de Oro
            125.9679, 126.0079, 125.9679, 125.9679, 125.9679, 126.0079, 125.9679, 126.0079, 125.9679, 126.0079,
            125.9679,
            // Davao del Norte
            125.8079, 125.6979, 125.7079, 125.8079, 125.8079, 125.8079, 125.8079, 125.8079, 125.8079, 125.8079,
            125.8079,
            // Davao del Sur
            125.3679, 125.6128, 125.3679, 125.3679, 125.3679, 125.3679, 125.3679, 125.3679, 125.3679, 125.3679,
            125.3679, 125.3679, 125.3679, 125.3679, 125.3679, 125.3679, 125.3679,
            // Davao Occidental
            125.3679, 125.3679, 125.3679, 125.3679, 125.3679,
            // Davao Oriental
            126.2379, 126.5679, 126.2379, 126.3679, 126.2379, 126.5679, 126.2379, 126.2379, 126.2379, 126.2379,
            126.2379
    };

    /**
     * Get all location labels for Region 11
     */
    public static String[] getRegion11Labels() {
        return REGION_11_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 11
     */
    public static double[] getRegion11Lats() {
        return REGION_11_LATS;
    }

    /**
     * Get all longitude coordinates for Region 11
     */
    public static double[] getRegion11Lons() {
        return REGION_11_LONS;
    }

    /**
     * Get location data for a specific index in Region 11
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion11Location(int index) {
        if (index >= 0 && index < REGION_11_LABELS.length) {
            return new LocationData(
                    REGION_11_LABELS[index],
                    REGION_11_LATS[index],
                    REGION_11_LONS[index]
            );
        }
        return null;
    }

    // Region 12 (Soccsksargen) - All cities and municipalities
    public static final String[] REGION_12_LABELS = {
            // Cotabato (North Cotabato)
            "Kidapawan, Cotabato",
            "Alamada, Cotabato",
            "Aleosan, Cotabato",
            "Antipas, Cotabato",
            "Arakan, Cotabato",
            "Banisilan, Cotabato",
            "Carmen, Cotabato",
            "Kabacan, Cotabato",
            "Libungan, Cotabato",
            "Magpet, Cotabato",
            "Makilala, Cotabato",
            "Matalam, Cotabato",
            "Midsayap, Cotabato",
            "M'lang, Cotabato",
            "Pigcawayan, Cotabato",
            "Pikit, Cotabato",
            "President Roxas, Cotabato",
            "Tulunan, Cotabato",
            // Sarangani
            "Alabel, Sarangani",
            "Glan, Sarangani",
            "Kiamba, Sarangani",
            "Maasim, Sarangani",
            "Maitum, Sarangani",
            "Malapatan, Sarangani",
            "Malungon, Sarangani",
            // South Cotabato
            "Koronadal, South Cotabato",
            "General Santos, South Cotabato",
            "Banga, South Cotabato",
            "Lake Sebu, South Cotabato",
            "Norala, South Cotabato",
            "Polomolok, South Cotabato",
            "Santo Niño, South Cotabato",
            "Surallah, South Cotabato",
            "T'boli, South Cotabato",
            "Tampakan, South Cotabato",
            "Tantangan, South Cotabato",
            "Tupi, South Cotabato",
            // Sultan Kudarat
            "Isulan, Sultan Kudarat",
            "Tacurong, Sultan Kudarat",
            "Bagumbayan, Sultan Kudarat",
            "Columbio, Sultan Kudarat",
            "Esperanza, Sultan Kudarat",
            "Kalamansig, Sultan Kudarat",
            "Lambayong, Sultan Kudarat",
            "Lebak, Sultan Kudarat",
            "Lutayan, Sultan Kudarat",
            "Palimbang, Sultan Kudarat",
            "President Quirino, Sultan Kudarat",
            "Senator Ninoy Aquino, Sultan Kudarat"
    };

    public static final double[] REGION_12_LATS = {
            // Cotabato (North Cotabato)
            7.0079, 7.4079, 7.1579, 7.2479, 7.4079, 7.5079, 7.2079, 7.1079, 7.2479, 7.1579,
            7.0079, 7.1079, 7.2079, 6.9479, 7.3079, 7.0579, 7.1579, 6.8379,
            // Sarangani
            5.8579, 5.8079, 5.9079, 5.8579, 6.0579, 5.9579, 6.3079,
            // South Cotabato
            6.5079, 6.1128, 6.5079, 6.2079, 6.5079, 6.2079, 6.5079, 6.2079, 6.2079, 6.5079,
            6.5079, 6.2079,
            // Sultan Kudarat
            6.6079, 6.6679, 6.5079, 6.7079, 6.7279, 6.5079, 6.8079, 6.6079, 6.6079, 6.2079,
            6.6079, 6.5079
    };

    public static final double[] REGION_12_LONS = {
            // Cotabato (North Cotabato)
            125.0079, 124.5579, 124.5579, 125.1279, 125.0079, 124.7079, 124.7079, 124.8079, 124.5079, 125.0079,
            125.0079, 124.9079, 124.5579, 124.9079, 124.4079, 124.6679, 125.0579, 124.9079,
            // Sarangani
            125.3079, 125.2079, 124.6079, 125.3079, 124.5079, 125.3079, 125.2079,
            // South Cotabato
            124.8579, 125.1628, 124.8579, 124.7079, 124.8579, 125.0079, 124.8579, 124.7079, 124.7079, 124.9579,
            124.8579, 124.9579,
            // Sultan Kudarat
            124.6079, 124.6079, 124.5579, 124.9079, 124.7079, 124.0079, 124.4079, 124.5079, 124.8079, 124.2079,
            124.6079, 124.3079
    };

    /**
     * Get all location labels for Region 12
     */
    public static String[] getRegion12Labels() {
        return REGION_12_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 12
     */
    public static double[] getRegion12Lats() {
        return REGION_12_LATS;
    }

    /**
     * Get all longitude coordinates for Region 12
     */
    public static double[] getRegion12Lons() {
        return REGION_12_LONS;
    }

    /**
     * Get location data for a specific index in Region 12
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion12Location(int index) {
        if (index >= 0 && index < REGION_12_LABELS.length) {
            return new LocationData(
                    REGION_12_LABELS[index],
                    REGION_12_LATS[index],
                    REGION_12_LONS[index]
            );
        }
        return null;
    }

    // Region 13 (Caraga) - All cities and municipalities
    public static final String[] REGION_13_LABELS = {
            // Agusan del Norte
            "Butuan, Agusan del Norte",
            "Cabadbaran, Agusan del Norte",
            "Buenavista, Agusan del Norte",
            "Carmen, Agusan del Norte",
            "Jabonga, Agusan del Norte",
            "Kitcharao, Agusan del Norte",
            "Las Nieves, Agusan del Norte",
            "Magallanes, Agusan del Norte",
            "Nasipit, Agusan del Norte",
            "Remedios T. Romualdez, Agusan del Norte",
            "Santiago, Agusan del Norte",
            "Tubay, Agusan del Norte",
            // Agusan del Sur
            "Prosperidad, Agusan del Sur",
            "Bayugan, Agusan del Sur",
            "Bunawan, Agusan del Sur",
            "Esperanza, Agusan del Sur",
            "La Paz, Agusan del Sur",
            "Loreto, Agusan del Sur",
            "Rosario, Agusan del Sur",
            "San Francisco, Agusan del Sur",
            "San Luis, Agusan del Sur",
            "Santa Josefa, Agusan del Sur",
            "Sibagat, Agusan del Sur",
            "Talacogon, Agusan del Sur",
            "Trento, Agusan del Sur",
            "Veruela, Agusan del Sur",
            // Dinagat Islands
            "San Jose, Dinagat Islands",
            "Basilisa, Dinagat Islands",
            "Cagdianao, Dinagat Islands",
            "Dinagat, Dinagat Islands",
            "Libjo, Dinagat Islands",
            "Loreto, Dinagat Islands",
            "Tubajon, Dinagat Islands",
            // Surigao del Norte
            "Surigao City, Surigao del Norte",
            "Alegria, Surigao del Norte",
            "Bacuag, Surigao del Norte",
            "Burgos, Surigao del Norte",
            "Claver, Surigao del Norte",
            "Dapa, Surigao del Norte",
            "Del Carmen, Surigao del Norte",
            "General Luna, Surigao del Norte",
            "Gigaquit, Surigao del Norte",
            "Mainit, Surigao del Norte",
            "Malimono, Surigao del Norte",
            "Pilar, Surigao del Norte",
            "Placer, Surigao del Norte",
            "San Benito, Surigao del Norte",
            "San Francisco, Surigao del Norte",
            "San Isidro, Surigao del Norte",
            "Santa Monica, Surigao del Norte",
            "Sison, Surigao del Norte",
            "Socorro, Surigao del Norte",
            "Tagana-an, Surigao del Norte",
            "Tubod, Surigao del Norte",
            // Surigao del Sur
            "Tandag, Surigao del Sur",
            "Bislig, Surigao del Sur",
            "Barobo, Surigao del Sur",
            "Bayabas, Surigao del Sur",
            "Cagwait, Surigao del Sur",
            "Cantilan, Surigao del Sur",
            "Carmen, Surigao del Sur",
            "Carrascal, Surigao del Sur",
            "Cortes, Surigao del Sur",
            "Hinatuan, Surigao del Sur",
            "Lanuza, Surigao del Sur",
            "Lianga, Surigao del Sur",
            "Lingig, Surigao del Sur",
            "Madrid, Surigao del Sur",
            "Marihatag, Surigao del Sur",
            "San Agustin, Surigao del Sur",
            "San Miguel, Surigao del Sur",
            "Tagbina, Surigao del Sur",
            "Tago, Surigao del Sur"
    };

    public static final double[] REGION_13_LATS = {
            // Agusan del Norte
            8.9479, 9.1079, 8.9479, 9.0079, 9.0079, 9.0079, 8.9479, 9.0079, 9.0079, 9.0079,
            9.0079, 9.0079,
            // Agusan del Sur
            8.6079, 8.7079, 8.2079, 8.6779, 8.0579, 8.1879, 8.3579, 8.5079, 8.5079, 8.5079,
            8.5079, 8.5079, 8.5079, 8.5079, 8.5079,
            // Dinagat Islands
            10.0079, 10.0079, 10.0079, 10.0079, 10.0079, 10.0079, 10.0079,
            // Surigao del Norte
            9.7879, 9.5079, 9.6079, 10.0079, 9.5079, 9.7579, 9.8579, 9.8579, 9.6079, 9.5079,
            9.6079, 9.8579, 9.6079, 9.8579, 9.8579, 9.8579, 9.8579, 9.8579, 9.8579, 9.8579,
            9.6079,
            // Surigao del Sur
            9.0779, 8.2079, 8.5079, 8.7079, 8.9179, 9.3379, 9.2079, 9.3579, 9.2779, 8.3579,
            9.2079, 8.6079, 8.0379, 9.2079, 8.8079, 8.9079, 8.9079, 8.4579, 9.0079
    };

    public static final double[] REGION_13_LONS = {
            // Agusan del Norte
            125.5479, 125.5379, 125.5479, 125.5479, 125.5479, 125.5479, 125.5479, 125.5479, 125.5479, 125.5479,
            125.5479, 125.5479,
            // Agusan del Sur
            125.9079, 125.7079, 125.9079, 125.9079, 125.8079, 125.9079, 125.9079, 125.7079, 125.7079, 125.7079,
            125.7079, 125.7079, 125.7079, 125.7079, 125.7079,
            // Dinagat Islands
            125.6079, 125.6079, 125.6079, 125.6079, 125.6079, 125.6079, 125.6079,
            // Surigao del Norte
            125.4879, 125.4879, 125.4879, 125.4879, 125.4879, 125.6079, 125.6079, 125.6079, 125.4879, 125.4879,
            125.4879, 125.6079, 125.4879, 125.6079, 125.6079, 125.6079, 125.6079, 125.6079, 125.6079, 125.6079,
            125.4879,
            // Surigao del Sur
            126.1979, 126.2079, 126.1979, 126.1979, 126.1979, 126.1979, 126.1979, 126.1979, 126.1979, 126.1979,
            126.1979, 126.1979, 126.1979, 126.1979, 126.1979, 126.1979, 126.1979, 126.1979, 126.1979
    };

    /**
     * Get all location labels for Region 13
     */
    public static String[] getRegion13Labels() {
        return REGION_13_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 13
     */
    public static double[] getRegion13Lats() {
        return REGION_13_LATS;
    }

    /**
     * Get all longitude coordinates for Region 13
     */
    public static double[] getRegion13Lons() {
        return REGION_13_LONS;
    }

    /**
     * Get location data for a specific index in Region 13
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion13Location(int index) {
        if (index >= 0 && index < REGION_13_LABELS.length) {
            return new LocationData(
                    REGION_13_LABELS[index],
                    REGION_13_LATS[index],
                    REGION_13_LONS[index]
            );
        }
        return null;
    }

    // Region 14 (NCR - Metro Manila) - All cities and municipality
    public static final String[] REGION_14_LABELS = {
            "Manila, Metro Manila",
            "Caloocan, Metro Manila",
            "Las Piñas, Metro Manila",
            "Makati, Metro Manila",
            "Malabon, Metro Manila",
            "Mandaluyong, Metro Manila",
            "Marikina, Metro Manila",
            "Muntinlupa, Metro Manila",
            "Navotas, Metro Manila",
            "Parañaque, Metro Manila",
            "Pasay, Metro Manila",
            "Pasig, Metro Manila",
            "Pateros, Metro Manila",
            "Quezon City, Metro Manila",
            "San Juan, Metro Manila",
            "Taguig, Metro Manila",
            "Valenzuela, Metro Manila"
    };

    public static final double[] REGION_14_LATS = {
            14.5995, 14.6548, 14.4495, 14.5502, 14.6568, 14.5822, 14.6507, 14.3833, 14.6667, 14.4793,
            14.5378, 14.5764, 14.5431, 14.6760, 14.6019, 14.5176, 14.7004
    };

    public static final double[] REGION_14_LONS = {
            120.9842, 120.9833, 120.9826, 121.0320, 120.9570, 121.0355, 121.1029, 121.0500, 120.9500, 121.0198,
            120.9924, 121.0851, 121.0675, 121.0437, 121.0433, 121.0509, 120.9833
    };

    /**
     * Get all location labels for Region 14
     */
    public static String[] getRegion14Labels() {
        return REGION_14_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 14
     */
    public static double[] getRegion14Lats() {
        return REGION_14_LATS;
    }

    /**
     * Get all longitude coordinates for Region 14
     */
    public static double[] getRegion14Lons() {
        return REGION_14_LONS;
    }

    /**
     * Get location data for a specific index in Region 14
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion14Location(int index) {
        if (index >= 0 && index < REGION_14_LABELS.length) {
            return new LocationData(
                    REGION_14_LABELS[index],
                    REGION_14_LATS[index],
                    REGION_14_LONS[index]
            );
        }
        return null;
    }

    // Region 15 (CAR - Cordillera Administrative Region) - All cities and municipalities
    public static final String[] REGION_15_LABELS = {
            // Abra
            "Bangued, Abra",
            "Boliney, Abra",
            "Bucay, Abra",
            "Bucloc, Abra",
            "Daguioman, Abra",
            "Danglas, Abra",
            "Dolores, Abra",
            "La Paz, Abra",
            "Lacub, Abra",
            "Lagangilang, Abra",
            "Lagayan, Abra",
            "Langiden, Abra",
            "Licuan-Baay, Abra",
            "Luba, Abra",
            "Malibcong, Abra",
            "Manabo, Abra",
            "Peñarrubia, Abra",
            "Pidigan, Abra",
            "Pilar, Abra",
            "Sallapadan, Abra",
            "San Isidro, Abra",
            "San Juan, Abra",
            "San Quintin, Abra",
            "Tayum, Abra",
            "Tineg, Abra",
            "Tubo, Abra",
            "Villaviciosa, Abra",
            // Apayao
            "Kabugao, Apayao",
            "Calanasan, Apayao",
            "Conner, Apayao",
            "Flora, Apayao",
            "Luna, Apayao",
            "Pudtol, Apayao",
            "Santa Marcela, Apayao",
            // Benguet
            "La Trinidad, Benguet",
            "Baguio, Benguet",
            "Atok, Benguet",
            "Bakun, Benguet",
            "Bokod, Benguet",
            "Buguias, Benguet",
            "Itogon, Benguet",
            "Kabayan, Benguet",
            "Kapangan, Benguet",
            "Kibungan, Benguet",
            "Mankayan, Benguet",
            "Sablan, Benguet",
            "Tuba, Benguet",
            "Tublay, Benguet",
            // Ifugao
            "Lagawe, Ifugao",
            "Aguinaldo, Ifugao",
            "Alfonso Lista, Ifugao",
            "Asipulo, Ifugao",
            "Banaue, Ifugao",
            "Hingyon, Ifugao",
            "Hungduan, Ifugao",
            "Kiangan, Ifugao",
            "Lamut, Ifugao",
            "Mayoyao, Ifugao",
            "Tinoc, Ifugao",
            // Kalinga
            "Tabuk, Kalinga",
            "Balbalan, Kalinga",
            "Lubuagan, Kalinga",
            "Pasil, Kalinga",
            "Pinukpuk, Kalinga",
            "Rizal, Kalinga",
            "Tanudan, Kalinga",
            "Tinglayan, Kalinga",
            // Mountain Province
            "Bontoc, Mountain Province",
            "Barlig, Mountain Province",
            "Bauko, Mountain Province",
            "Besao, Mountain Province",
            "Natonin, Mountain Province",
            "Paracelis, Mountain Province",
            "Sabangan, Mountain Province",
            "Sadanga, Mountain Province",
            "Sagada, Mountain Province",
            "Tadian, Mountain Province"
    };

    public static final double[] REGION_15_LATS = {
            // Abra
            17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080,
            17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080,
            17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080, 17.6080,
            // Apayao
            18.0080, 18.0080, 17.8080, 18.0080, 18.0080, 18.0080, 18.0080,
            // Benguet
            16.4580, 16.4028, 16.5580, 16.7580, 16.4580, 16.6580, 16.3580, 16.6580, 16.5580, 16.6580,
            16.8580, 16.4580, 16.3580, 16.4580,
            // Ifugao
            16.8080, 16.8080, 16.8080, 16.8080, 16.9080, 16.8080, 16.8080, 16.8080, 16.8080, 16.8080,
            16.8080,
            // Kalinga
            17.4080, 17.4080, 17.4080, 17.4080, 17.4080, 17.4080, 17.4080, 17.4080,
            // Mountain Province
            17.1080, 17.1080, 17.0080, 17.1080, 17.1080, 17.1080, 17.1080, 17.1080, 17.1080, 17.0080
    };

    public static final double[] REGION_15_LONS = {
            // Abra
            120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180,
            120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180,
            120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180, 120.6180,
            // Apayao
            121.1180, 121.1180, 121.1180, 121.1180, 121.1180, 121.1180, 121.1180,
            // Benguet
            120.5880, 120.5960, 120.6880, 120.6880, 120.7880, 120.7880, 120.5880, 120.7880, 120.5880, 120.6880,
            120.7880, 120.4880, 120.5880, 120.5880,
            // Ifugao
            121.1180, 121.1180, 121.1180, 121.1180, 121.1180, 121.1180, 121.1180, 121.1180, 121.1180, 121.1180,
            121.1180,
            // Kalinga
            121.4180, 121.4180, 121.4180, 121.4180, 121.4180, 121.4180, 121.4180, 121.4180,
            // Mountain Province
            121.0180, 121.0180, 120.8180, 121.0180, 121.0180, 121.0180, 121.0180, 121.0180, 120.9180, 120.8180
    };

    /**
     * Get all location labels for Region 15
     */
    public static String[] getRegion15Labels() {
        return REGION_15_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 15
     */
    public static double[] getRegion15Lats() {
        return REGION_15_LATS;
    }

    /**
     * Get all longitude coordinates for Region 15
     */
    public static double[] getRegion15Lons() {
        return REGION_15_LONS;
    }

    /**
     * Get location data for a specific index in Region 15
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion15Location(int index) {
        if (index >= 0 && index < REGION_15_LABELS.length) {
            return new LocationData(
                    REGION_15_LABELS[index],
                    REGION_15_LATS[index],
                    REGION_15_LONS[index]
            );
        }
        return null;
    }

    // Region 16 (BARMM - Bangsamoro Autonomous Region in Muslim Mindanao) - All cities and municipalities
    public static final String[] REGION_16_LABELS = {
            // Basilan
            "Isabela, Basilan",
            "Lamitan, Basilan",
            "Akbar, Basilan",
            "Al-Barka, Basilan",
            "Hadji Mohammad Ajul, Basilan",
            "Hadji Muhtamad, Basilan",
            "Lantawan, Basilan",
            "Maluso, Basilan",
            "Sumisip, Basilan",
            "Tabuan-Lasa, Basilan",
            "Tipo-Tipo, Basilan",
            "Tuburan, Basilan",
            "Ungkaya Pukan, Basilan",
            // Lanao del Sur
            "Marawi, Lanao del Sur",
            "Bacolod-Kalawi, Lanao del Sur",
            "Balabagan, Lanao del Sur",
            "Balindong, Lanao del Sur",
            "Bayang, Lanao del Sur",
            "Binidayan, Lanao del Sur",
            "Bubong, Lanao del Sur",
            "Butig, Lanao del Sur",
            "Calanogas, Lanao del Sur",
            "Ditsaan-Ramain, Lanao del Sur",
            "Ganassi, Lanao del Sur",
            "Kapai, Lanao del Sur",
            "Kapatagan, Lanao del Sur",
            "Lumba-Bayabao, Lanao del Sur",
            "Lumbaca-Unayan, Lanao del Sur",
            "Lumbatan, Lanao del Sur",
            "Lumbayanague, Lanao del Sur",
            "Madalum, Lanao del Sur",
            "Madamba, Lanao del Sur",
            "Maguing, Lanao del Sur",
            "Malabang, Lanao del Sur",
            "Marantao, Lanao del Sur",
            "Marogong, Lanao del Sur",
            "Masiu, Lanao del Sur",
            "Mulondo, Lanao del Sur",
            "Pagayawan, Lanao del Sur",
            "Piagapo, Lanao del Sur",
            "Picong, Lanao del Sur",
            "Poona Bayabao, Lanao del Sur",
            "Pualas, Lanao del Sur",
            "Saguiaran, Lanao del Sur",
            "Sultan Dumalondong, Lanao del Sur",
            "Tagoloan II, Lanao del Sur",
            "Tamparan, Lanao del Sur",
            "Taraka, Lanao del Sur",
            "Tubaran, Lanao del Sur",
            "Tugaya, Lanao del Sur",
            "Wao, Lanao del Sur",
            // Maguindanao del Norte
            "Datu Odin Sinsuat, Maguindanao del Norte",
            "Barira, Maguindanao del Norte",
            "Buldon, Maguindanao del Norte",
            "Datu Blah T. Sinsuat, Maguindanao del Norte",
            "Kabuntalan, Maguindanao del Norte",
            "Matanog, Maguindanao del Norte",
            "Northern Kabuntalan, Maguindanao del Norte",
            "Parang, Maguindanao del Norte",
            "Sultan Kudarat, Maguindanao del Norte",
            "Sultan Mastura, Maguindanao del Norte",
            "Sultan Sumagka, Maguindanao del Norte",
            "Talitay, Maguindanao del Norte",
            "Upi, Maguindanao del Norte",
            // Maguindanao del Sur
            "Buluan, Maguindanao del Sur",
            "Datu Paglas, Maguindanao del Sur",
            "Datu Piang, Maguindanao del Sur",
            "Datu Salibo, Maguindanao del Sur",
            "Datu Saudi-Ampatuan, Maguindanao del Sur",
            "Datu Unsay, Maguindanao del Sur",
            "General Salipada K. Pendatun, Maguindanao del Sur",
            "Guindulungan, Maguindanao del Sur",
            "Mamasapano, Maguindanao del Sur",
            "Mangudadatu, Maguindanao del Sur",
            "Pagalungan, Maguindanao del Sur",
            "Paglat, Maguindanao del Sur",
            "Pandag, Maguindanao del Sur",
            "Rajah Buayan, Maguindanao del Sur",
            "Shariff Aguak, Maguindanao del Sur",
            "Shariff Saydona Mustapha, Maguindanao del Sur",
            "South Upi, Maguindanao del Sur",
            "Sultan sa Barongis, Maguindanao del Sur",
            "Talayan, Maguindanao del Sur",
            // Sulu
            "Jolo, Sulu",
            "Banguingui, Sulu",
            "Hadji Panglima Tahil, Sulu",
            "Indanan, Sulu",
            "Kalingalan Caluang, Sulu",
            "Lugus, Sulu",
            "Luuk, Sulu",
            "Maimbung, Sulu",
            "Old Panamao, Sulu",
            "Omar, Sulu",
            "Pandami, Sulu",
            "Panglima Estino, Sulu",
            "Pangutaran, Sulu",
            "Parang, Sulu",
            "Pata, Sulu",
            "Patikul, Sulu",
            "Siasi, Sulu",
            "Talipao, Sulu",
            "Tapul, Sulu",
            // Tawi-Tawi
            "Bongao, Tawi-Tawi",
            "Languyan, Tawi-Tawi",
            "Mapun, Tawi-Tawi",
            "Panglima Sugala, Tawi-Tawi",
            "Sapa-Sapa, Tawi-Tawi",
            "Sibutu, Tawi-Tawi",
            "Simunul, Tawi-Tawi",
            "Sitangkai, Tawi-Tawi",
            "South Ubian, Tawi-Tawi",
            "Tandubas, Tawi-Tawi",
            "Turtle Islands, Tawi-Tawi",
            // Cotabato City
            "Cotabato City, BARMM"
    };

    public static final double[] REGION_16_LATS = {
            // Basilan
            6.7045, 6.6539, 6.6539, 6.6539, 6.6539, 6.6539, 6.6539, 6.5431, 6.4197, 6.4197,
            6.5097, 6.5097, 6.5097,
            // Lanao del Sur
            8.0014, 7.8569, 7.5261, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569,
            7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569,
            7.8569, 7.5261, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569,
            7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569, 7.8569,
            7.8569,
            // Maguindanao del Norte
            7.1881, 7.4872, 7.4872, 7.4872, 7.1167, 7.4872, 7.1167, 7.3733, 7.2392, 7.2392,
            7.2392, 7.2392, 7.0333,
            // Maguindanao del Sur
            6.7167, 7.1167, 7.1167, 7.1167, 6.9167, 7.1167, 7.1167, 7.1167, 7.1167, 6.7167,
            7.1167, 6.7167, 6.7167, 6.7167, 6.9167, 6.7167, 6.7167, 6.7167, 6.7167,
            // Sulu
            6.0522, 6.0522, 6.0522, 6.0522, 6.0522, 6.0522, 6.0522, 6.0522, 6.0522, 6.0522,
            6.0522, 6.0522, 6.0522, 6.0522, 6.0522, 6.0522, 6.0522, 6.0522, 6.0522,
            // Tawi-Tawi
            5.0292, 5.0292, 5.0292, 5.0292, 5.0292, 5.0292, 5.0292, 5.0292, 5.0292, 5.0292,
            5.0292,
            // Cotabato City
            7.2167
    };

    public static final double[] REGION_16_LONS = {
            // Basilan
            121.9715, 122.1436, 121.9715, 121.9715, 121.9715, 121.9715, 121.9715, 121.8815, 121.9715, 121.9715,
            121.9715, 121.9715, 121.9715,
            // Lanao del Sur
            124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958,
            124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958,
            124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958,
            124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958, 124.2958,
            124.2958,
            // Maguindanao del Norte
            124.2633, 124.2633, 124.2633, 124.2633, 124.2633, 124.2633, 124.2633, 124.2633, 124.2633, 124.2633,
            124.2633, 124.2633, 124.2633,
            // Maguindanao del Sur
            124.8833, 124.8833, 124.8833, 124.8833, 124.8833, 124.8833, 124.8833, 124.8833, 124.8833, 124.8833,
            124.8833, 124.8833, 124.8833, 124.8833, 124.8833, 124.8833, 124.8833, 124.8833, 124.8833,
            // Sulu
            121.1022, 121.1022, 121.1022, 121.1022, 121.1022, 121.1022, 121.1022, 121.1022, 121.1022, 121.1022,
            121.1022, 121.1022, 121.1022, 121.1022, 121.1022, 121.1022, 121.1022, 121.1022, 121.1022,
            // Tawi-Tawi
            119.7733, 119.7733, 119.7733, 119.7733, 119.7733, 119.7733, 119.7733, 119.7733, 119.7733, 119.7733,
            119.7733,
            // Cotabato City
            124.2450
    };

    /**
     * Get all location labels for Region 16
     */
    public static String[] getRegion16Labels() {
        return REGION_16_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 16
     */
    public static double[] getRegion16Lats() {
        return REGION_16_LATS;
    }

    /**
     * Get all longitude coordinates for Region 16
     */
    public static double[] getRegion16Lons() {
        return REGION_16_LONS;
    }

    /**
     * Get location data for a specific index in Region 16
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion16Location(int index) {
        if (index >= 0 && index < REGION_16_LABELS.length) {
            return new LocationData(
                    REGION_16_LABELS[index],
                    REGION_16_LATS[index],
                    REGION_16_LONS[index]
            );
        }
        return null;
    }

    // Region 17 - Placeholder for future use
    public static final String[] REGION_17_LABELS = {
            // Empty - can be populated later
    };

    public static final double[] REGION_17_LATS = {
            // Empty - can be populated later
    };

    public static final double[] REGION_17_LONS = {
            // Empty - can be populated later
    };

    /**
     * Get all location labels for Region 17
     */
    public static String[] getRegion17Labels() {
        return REGION_17_LABELS;
    }

    /**
     * Get all latitude coordinates for Region 17
     */
    public static double[] getRegion17Lats() {
        return REGION_17_LATS;
    }

    /**
     * Get all longitude coordinates for Region 17
     */
    public static double[] getRegion17Lons() {
        return REGION_17_LONS;
    }

    /**
     * Get location data for a specific index in Region 17
     * @param index The index of the location
     * @return LocationData object containing name, latitude, and longitude, or null if index is invalid
     */
    public static LocationData getRegion17Location(int index) {
        if (index >= 0 && index < REGION_17_LABELS.length) {
            return new LocationData(
                    REGION_17_LABELS[index],
                    REGION_17_LATS[index],
                    REGION_17_LONS[index]
            );
        }
        return null;
    }

    /**
     * Simple data class to hold location information
     */
    public static class LocationData {
        public final String name;
        public final double latitude;
        public final double longitude;

        public LocationData(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
    
    /**
     * Get all available locations across all regions
     */
    public static String[] getAllLocations() {
        List<String> allLocations = new ArrayList<>();
        allLocations.addAll(Arrays.asList(REGION_1_LABELS));
        allLocations.addAll(Arrays.asList(REGION_2_LABELS));
        allLocations.addAll(Arrays.asList(REGION_3_LABELS));
        allLocations.addAll(Arrays.asList(REGION_4_LABELS));
        allLocations.addAll(Arrays.asList(REGION_5_LABELS));
        allLocations.addAll(Arrays.asList(REGION_6_LABELS));
        allLocations.addAll(Arrays.asList(REGION_7_LABELS));
        allLocations.addAll(Arrays.asList(REGION_8_LABELS));
        allLocations.addAll(Arrays.asList(REGION_9_LABELS));
        allLocations.addAll(Arrays.asList(REGION_10_LABELS));
        allLocations.addAll(Arrays.asList(REGION_11_LABELS));
        allLocations.addAll(Arrays.asList(REGION_12_LABELS));
        allLocations.addAll(Arrays.asList(REGION_13_LABELS));
        return allLocations.toArray(new String[0]);
    }
}

