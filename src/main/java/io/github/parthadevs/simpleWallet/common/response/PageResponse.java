package io.github.parthadevs.simpleWallet.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {

    private List<T> data;
    private Meta meta;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Meta {
        @JsonProperty("current_page")
        private int currentPage;

        @JsonProperty("per_page")
        private int perPage;

        private long total;

        @JsonProperty("last_page")
        private int lastPage;
    }
}