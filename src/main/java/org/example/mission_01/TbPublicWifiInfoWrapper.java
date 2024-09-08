package org.example.mission_01;

import lombok.Data;
import java.util.List;


@Data
public class TbPublicWifiInfoWrapper {
    private TbPublicWifiInfo TbPublicWifiInfo;
}

@Data
class TbPublicWifiInfo {
    private int list_total_count;
    private RESULT RESULT;
    private List<ROW> row;
}

@Data
class RESULT {
    private String CODE;
    private String MESSAGE;
}

@Data
class ROW {
    private double distance;
    private String X_SWIFI_MGR_NO;
    private String X_SWIFI_WRDOFC;
    private String X_SWIFI_MAIN_NM;
    private String X_SWIFI_ADRES1;
    private String X_SWIFI_ADRES2;
    private String X_SWIFI_INSTL_FLOOR;
    private String X_SWIFI_INSTL_TY;
    private String X_SWIFI_INSTL_MBY;
    private String X_SWIFI_SVC_SE;
    private String X_SWIFI_CMCWR;
    private String X_SWIFI_CNSTC_YEAR;
    private String X_SWIFI_INOUT_DOOR;
    private String X_SWIFI_REMARS3;
    private String LAT;
    private String LNT;
    private String WORK_DTTM;
}