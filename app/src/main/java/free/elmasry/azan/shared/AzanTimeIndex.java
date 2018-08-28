package free.elmasry.azan.shared;

public interface AzanTimeIndex {
    /*
     * these variables are very important it's used in many classes and all arrays related to
     * azan times are built based on them. we assumed when writing the code for this app, any array
     * contains these times it will order ascending according to the time
     */
    int ALL_TIMES_NUM = 6;
    int INDEX_FAJR = 0;
    int INDEX_SHUROOQ = 1;
    int INDEX_DHUHR = 2;
    int INDEX_ASR = 3;
    int INDEX_MAGHRIB = 4;
    int INDEX_ISHAA = 5;
}
