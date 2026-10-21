package com.boit.boit_leave.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarEventDto {

	    private String title;

	    private LocalDate start;

	    private LocalDate end;

	    private String type;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public LocalDate getStart() {
			return start;
		}

		public void setStart(LocalDate start) {
			this.start = start;
		}

		public LocalDate getEnd() {
			return end;
		}

		public void setEnd(LocalDate end) {
			this.end = end;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	    
	    
	}


