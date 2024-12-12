package com.elighthouse.ndtnet2plan.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class KpiMetricGivenLinkId {
	@JsonProperty("link-id") public String linkId;
	@JsonProperty("value") public double value;
	@JsonProperty("unit") public String unit;
	
	public KpiMetricGivenLinkId() {}
	
	public KpiMetricGivenLinkId(String linkId, double value, String unit) {
		this.linkId = linkId;
		this.value = value;
		this.unit = unit;
	}
	
	public static KpiMetricGivenLinkId ofGbps(String linkId, double value) {
		return new KpiMetricGivenLinkId(linkId, value, Unit.GBPS.toString());
	}
	
	public static KpiMetricGivenLinkId ofMs(String linkId, double value) {
		return new KpiMetricGivenLinkId(linkId, value, Unit.MS.toString());
	}
	
	public enum Unit {
        @JsonProperty("ms") MS,
        @JsonProperty("Gbps") GBPS;

        @JsonCreator
        public static Unit fromString(String key) {
            return key == null ? null : Unit.valueOf(key.toUpperCase());
        }
    }
}
