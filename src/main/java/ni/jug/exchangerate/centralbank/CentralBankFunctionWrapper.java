package ni.jug.exchangerate.centralbank;

import ni.jug.exchangerate.ExchangeRateException;

/**
 * @author aalaniz
 */
public interface CentralBankFunctionWrapper<I, O> {

    O apply(I i) throws ExchangeRateException;
}
