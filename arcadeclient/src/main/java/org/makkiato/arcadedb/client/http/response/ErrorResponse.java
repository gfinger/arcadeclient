package org.makkiato.arcadedb.client.http.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ErrorResponse {
    private String error;
    private String detail;
    private String exception;
    private String exceptionArgs;

    public ErrorResponse(String error, String detail, String exception) {
        this.error = error;
        this.detail = detail;
        this.exception = exception;
    }

    public ErrorResponse(String error, String detail, String exception, String exceptionArgs) {
        this.error = error;
        this.detail = detail;
        this.exception = exception;
        this.exceptionArgs = exceptionArgs;
    }
}
