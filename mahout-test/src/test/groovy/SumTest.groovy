import com.liangjidong.test.spock.Sum
import spock.lang.Specification

/**
 * Created by author on 17-12-3.
 */
class SumTest extends Specification {

    def sum = new Sum();

    void setup() {
    }

    void cleanup() {
    }

    def "Sum should return a+b"() {
        expect:
        sum.sum(1, 1) == 2

        sum.sum(a, b) == c

        where:
        a  | b || c
        1  | 1 || 2
        2  | 2 || 4
        -1 | 2 || 1
    }
}
