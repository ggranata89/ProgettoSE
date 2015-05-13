package com.example.ricky.mycity;

/**
 * Created by giuseppe on 12/05/15.
 */
public enum PriorityEnum {
    NONE("1"),MINOR("2"),NORMAL("3"),CRITICAL("4");

    private String priority;

    PriorityEnum(String priority){
        this.priority=priority;
    }
}
