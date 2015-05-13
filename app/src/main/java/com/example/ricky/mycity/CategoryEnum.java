package com.example.ricky.mycity;

/**
 * Created by giuseppe on 12/05/15.
 */
public enum CategoryEnum {
    WASTE_MANAGEMENT("1"),
    ROUTINE_MAINTENANCE("2"),
    ROAD_SIGN("3"),
    VANDALISM("4"),
    ILLEGAL_BILLPOSTING("5"),
    OTHER("6");

    private String category;

    CategoryEnum(String category){
        this.category=category;
    }

}
