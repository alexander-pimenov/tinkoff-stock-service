package com.pimalex.tinkoff.exceptions;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class ErrorDto {
    String error;
}
