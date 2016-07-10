package hacker.service.parser;

import java.util.List;

import hacker.model.CashFlow;

/**
 * Created by sarath on 7/10/16.
 */
public interface Parser {

    CashFlow parse(CashFlow cashFlow);
}
