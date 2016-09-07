package redx.mit.edu.spectrometer;

/**
 * Created by Ishan on 18-06-2015.
 */
public class DatabaseDataFormat {
    
    private int row;
    private String name;
    private String spectrum0;
    private String spectrum1;
    private String spectrum2;
    private String spectrum3;
    private String darkReading;

    public DatabaseDataFormat() {

    }

    public DatabaseDataFormat(int row,String name, String spectrum0,
            String spectrum1, String spectrum2, String spectrum3,
                              String darkReading) {
        this.row = row;
        this.name = name;
        this.spectrum0 = spectrum0;
        this.spectrum1 = spectrum1;
        this.spectrum2 = spectrum2;
        this.spectrum3 = spectrum3;
        this.darkReading = darkReading;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getSpectrum3() {
        return spectrum3;
    }

    public void setSpectrum3(String spectrum3) {
        this.spectrum3 = spectrum3;
    }

    public String getSpectrum2() {
        return spectrum2;
    }

    public void setSpectrum2(String spectrum2) {
        this.spectrum2 = spectrum2;
    }

    public String getSpectrum1() {
        return spectrum1;
    }

    public void setSpectrum1(String spectrum1) {
        this.spectrum1 = spectrum1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpectrum0() {
        return spectrum0;
    }

    public void setSpectrum0(String spectrum0) {
        this.spectrum0 = spectrum0;
    }

    public void setDarkReading(String darkReading){
        this.darkReading = darkReading;
    }

    public String getDarkReading(){
        return darkReading;
    }

    @Override
    public String toString() {
        return "DatabaseDataFormat{" +
                "row=" + row +
                ", name='" + name + '\'' +
                ", spectrum0='" + spectrum0 + '\'' +
                ", spectrum1='" + spectrum1 + '\'' +
                ", spectrum2='" + spectrum2 + '\'' +
                ", spectrum3='" + spectrum3 + '\'' +
                ", darkSpectrum='" + darkReading + '\'' +
                '}';
    }
}
