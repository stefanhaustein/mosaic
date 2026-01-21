package org.kobjects.mosaic.svg.parser

import org.kobjects.parsek.tokenizer.Scanner
import org.kobjects.parsek.tokenizer.RegularExpressions

class ExpressionScanner(
    input: String
) : Scanner<ExpressionTokenType>(
    input,
    ExpressionTokenType.EOF,
    RegularExpressions.WHITESPACE to null,
    RegularExpressions.IDENTIFIER to ExpressionTokenType.IDENTIFIER,
    RegularExpressions.NUMBER to ExpressionTokenType.NUMBER,
    RegularExpressions.DOUBLE_QUOTED_STRING to ExpressionTokenType.STRING,
    Regex("""'([^'\\]*(\\.[^'\\]*)*)'""") to ExpressionTokenType.STRING,
    RegularExpressions.SYMBOL to ExpressionTokenType.SYMBOL
) {
}