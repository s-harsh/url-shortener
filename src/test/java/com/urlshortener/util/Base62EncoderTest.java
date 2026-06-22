package com.urlshortener.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Base62EncoderTest {

    @Test
    void encode_zero_returns_zero_char() {
        assertThat(Base62Encoder.encode(0)).isEqualTo("0");
    }

    @Test
    void encode_one_returns_one_char() {
        assertThat(Base62Encoder.encode(1)).isEqualTo("1");
    }

    @Test
    void encode_62_returns_a() {
        assertThat(Base62Encoder.encode(10)).isEqualTo("a");
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 100L, 999_999L, 3_521_614_606_208L})
    void encode_then_decode_is_identity(long number) {
        String encoded = Base62Encoder.encode(number);
        long decoded = Base62Encoder.decode(encoded);
        assertThat(decoded).isEqualTo(number);
    }

    @Test
    void encode_negative_throws() {
        assertThatThrownBy(() -> Base62Encoder.encode(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void encodeWithPadding_pads_to_target_length() {
        String result = Base62Encoder.encodeWithPadding(1, 7);
        assertThat(result).hasSize(7).startsWith("000000");
    }

    @Test
    void encodeWithPadding_does_not_truncate_longer() {
        long large = 62L * 62L * 62L * 62L * 62L * 62L * 62L + 1; // > 7 chars
        String result = Base62Encoder.encodeWithPadding(large, 7);
        assertThat(result.length()).isGreaterThanOrEqualTo(7);
    }
}
