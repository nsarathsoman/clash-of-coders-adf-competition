package hacker.dao;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import hacker.model.CashFlow;
import hacker.UndertowServer;

/**
 * Created by sarath on 7/4/16.
 */
public class CashFlowDAO {

    private static final String TABLE = "cash_flow";
    private static final String KEY = "key";
    private static final String DL_FILE_NAME = "dl_file_name";
    private static final String CASH_FLOW = "cash_flow";
    private static final String BANK_NAME = "bank_name";
    private static final HikariDataSource dataSource = UndertowServer.HIKARI;

    private static final CashFlowDAO cashFlowDAO = new CashFlowDAO();
    private static Map<String, CashFlow> cashFlowMap;

    public static CashFlowDAO getInstance() {
        return cashFlowDAO;
    }

    private CashFlowDAO(){}

    public List<CashFlow> getCashFlows(List<String> keys) {
        LocalTime startTime = LocalTime.now();
        List<CashFlow> cashFlows = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try(Connection connection = dataSource.getConnection()){
            StringBuilder sb = new StringBuilder();
            Iterator<String> keysIterator = keys.iterator();
            int i = 0;
            while(keysIterator.hasNext()){
                sb.append("'");
                sb.append(keysIterator.next());
                sb.append("'");
                if(++i != keys.size()){
                    sb.append(",");
                }
            }
            String keysString = sb.toString();
            String query = "select `" + DL_FILE_NAME + "`, `"+KEY+"` from " + TABLE + " where `key` in ( " + keysString+ " );";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                cashFlows.add(
                        CashFlow.CashFlowBuilder
                                .build()
                                .withDLFileName(resultSet.getString(DL_FILE_NAME))
                                .withKey(resultSet.getString(KEY))
                                .done()
                );
            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            if(null != resultSet) {
                try {
                    resultSet.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            if(null != preparedStatement) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        System.out.println("FetchKey Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()) + "ms");
        return cashFlows;
    }

    public void persistCashFlow(CashFlow cashFlow) {
        LocalTime startTime = LocalTime.now();
        PreparedStatement preparedStatement = null;
        try(Connection connection = dataSource.getConnection()){
            String query = "update " + TABLE + " set "+ CASH_FLOW +"=? where `key` = ?;";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, String.valueOf(cashFlow.getCashFlow()));
            preparedStatement.setString(2, cashFlow.getKey());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            if(null != preparedStatement) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        System.out.println("Persist CashFlow Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()) + "ms");
    }

    public void persistBankName(CashFlow cashFlow) {
        LocalTime startTime = LocalTime.now();
        PreparedStatement preparedStatement = null;
        try (Connection connection = dataSource.getConnection()){
            String query = "update " + TABLE + " set "+ BANK_NAME +"=? where `key` = ?;";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, cashFlow.getBankName());
            preparedStatement.setString(2, cashFlow.getKey());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            if(null != preparedStatement) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        System.out.println("Persist BankName Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()) + "ms");
    }

    public void updateBankNameAndCashFlow(Collection<CashFlow> cashFlows) {
        LocalTime startTime = LocalTime.now();
//        cashFlows.forEach((cashFlow)-> updateBankNameAndCashFlow(cashFlow));
        updateBankNameAndCashFlowInBatch(cashFlows);
        System.out.println("Persist BankName and CashFlow Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()) + "ms");
    }


    private void updateBankNameAndCashFlowInBatch(Collection<CashFlow> cashFlows) {
        LocalTime startTime = LocalTime.now();
        PreparedStatement preparedStatement = null;
        try (Connection connection = dataSource.getConnection()) {
//            connection.setAutoCommit(false);
            String query = "update " + TABLE + " set " + BANK_NAME + "=?, " + CASH_FLOW + "=? where `key` = ?;";
            preparedStatement = connection.prepareStatement(query);
            PreparedStatement finalPreparedStatement = preparedStatement;
            cashFlows.forEach(cashFlow -> {
                System.out.println("Persiting cashflow :" + cashFlow.getCashFlow() + " " + cashFlow.getBankName());
                try {
                    finalPreparedStatement.setString(1, cashFlow.getBankName());
                    finalPreparedStatement.setString(2, String.valueOf(cashFlow.getCashFlow()));
                    finalPreparedStatement.setString(3, cashFlow.getKey());
//                    finalPreparedStatement.executeUpdate();
                    finalPreparedStatement.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            int[] rowsAffected = finalPreparedStatement.executeBatch();
//            connection.commit();
            System.out.print("Batch update. Rows affected " + rowsAffected.length);
            System.out.println("Persist BankName and CashFlow Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()) + "ms");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(null != preparedStatement) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateBankNameAndCashFlow(final CashFlow cashFlow) {
        PreparedStatement preparedStatement = null;
        try (Connection connection = dataSource.getConnection()) {
//            connection.setAutoCommit(false);
            String query = "update " + TABLE + " set " + BANK_NAME + "=?, " + CASH_FLOW + "=? where `key` = ?;";
            preparedStatement = connection.prepareStatement(query);
                System.out.println("Persiting cashflow :" + cashFlow.getCashFlow() + " " + cashFlow.getBankName());
            try {
                preparedStatement.setString(1, cashFlow.getBankName());
                preparedStatement.setString(2, String.valueOf(cashFlow.getCashFlow()));
                preparedStatement.setString(3, cashFlow.getKey());
                preparedStatement.executeUpdate();
//                            finalPreparedStatement.addBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
//                        finalPreparedStatement.executeBatch();
//            connection.commit();
            preparedStatement.close();
        } catch (SQLException e) {
            if (null != preparedStatement) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    public Map<String, CashFlow> loadCashFlowMap() {
        Map<String, CashFlow> cashFlowsMap = new HashMap<>();
        LocalTime startTime = LocalTime.now();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try(Connection connection = dataSource.getConnection()){
            String query = "select `" + DL_FILE_NAME + "`, `"+KEY+"` from " + TABLE + ";";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final String key = resultSet.getString(KEY);
                cashFlowsMap.put(key, CashFlow.CashFlowBuilder
                        .build()
                        .withDLFileName(resultSet.getString(DL_FILE_NAME))
                        .withKey(key)
                        .done());

            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            if(null != resultSet) {
                try {
                    resultSet.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            if(null != preparedStatement) {
                try {
                    preparedStatement.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        System.out.println("FetchAll Time : " + ChronoUnit.MILLIS.between(startTime, LocalTime.now()) + "ms");
        return cashFlowsMap;
    }

    public void initCashFlowMap() {
       cashFlowMap = loadCashFlowMap();
    }

    public Map<String, CashFlow> getCashFlowMap() {
        return cashFlowMap;
    }
}
