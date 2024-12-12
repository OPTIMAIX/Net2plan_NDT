package com.elighthouse.ndtnet2plan.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class IetfTvrTopologySchedule {
	
	@JsonProperty("ietf-tvr-topology:topology-schedule")
    public TopologyScheduleContent topologySchedule;

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
    public static class TopologyScheduleContent {
        @JsonProperty("nodes")
        public List<Node> nodes;
        
        @JsonProperty("links")
        public List<Link> links;
    }
    
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
    public static class Node {
        @JsonProperty("node-id")
        public String nodeId;
        
        @JsonProperty("available")
        public Availability available;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
    public static class Link {
        @JsonProperty("source-node")
        public String sourceNode;
        
        @JsonProperty("source-link-id")
        public String sourceLinkId;
        
        @JsonProperty("available")
        public Availability available;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
    public static class Availability {
        @JsonProperty("default-node-available")
        public Boolean defaultNodeAvailable;

        @JsonProperty("default-link-available")
        public Boolean defaultLinkAvailable;

        @JsonProperty("schedules")
        public List<Schedule> schedules;
        
        @JsonProperty("time-zone-identifier")
        public String timeZoneIdentifier;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
    public static class Schedule {
        @JsonProperty("schedule-id")
        public Integer scheduleId;
        
        @JsonProperty("schedule-type")
        public ScheduleType scheduleType;
        
        @JsonProperty("attr-value")
        public AttrValue attrValue;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
    public static class ScheduleType {
        @JsonProperty("recurrence")
        public Recurrence recurrence;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
    public static class Recurrence {
        @JsonProperty("recurrence-first")
        public RecurrenceFirst recurrenceFirst;
        
        @JsonProperty("recurrence-bound")
        public RecurrenceBound recurrenceBound;
        
        @JsonProperty("frequency")
        public Integer frequency;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
    public static class RecurrenceFirst {
        @JsonProperty("utc-start-time")
        public String utcStartTime;
        
        @JsonProperty("duration")
        public Integer duration;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
    public static class RecurrenceBound {
        @JsonProperty("count")
        public Integer count;
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
    public static class AttrValue {
        @JsonProperty("node-available")
        public Boolean nodeAvailable;

        @JsonProperty("link-available")
        public Boolean linkAvailable;
    }
}
