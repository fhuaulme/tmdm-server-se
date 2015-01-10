package com.amalto.core.server;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.routing.*;
import com.amalto.core.util.RoutingException;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;
import org.apache.log4j.Logger;
import com.amalto.core.server.api.RoutingOrder;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


public class DefaultRoutingOrder implements RoutingOrder {

    private final static String LOGGING_EVENT = "logging_event"; //$NON-NLS-1$

    private final static long DELAY = 5;  //time after which the send is accomplished asynchronously TODO: move to conf file

    private final static Logger LOGGER = Logger.getLogger(DefaultRoutingOrder.class);

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss,SSS"); //$NON-NLS-1$

    /**
     * Executes a Routing Order now
     */
    public String executeSynchronously(AbstractRoutingOrderV2POJO routingOrderPOJO) throws XtentisException {
        return executeSynchronously(routingOrderPOJO, true);
    }

    /**
     * Executes a Routing Order now
     */
    public String executeSynchronously(AbstractRoutingOrderV2POJO routingOrderPOJO, boolean cleanUpRoutingOrder) throws XtentisException {
        switch (routingOrderPOJO.getStatus()) {
            case AbstractRoutingOrderV2POJO.ACTIVE:
                //run as designed
                break;
            case AbstractRoutingOrderV2POJO.COMPLETED:
            case AbstractRoutingOrderV2POJO.FAILED:
                //create an active routing order
                ActiveRoutingOrderV2POJO activeRO = new ActiveRoutingOrderV2POJO(
                        routingOrderPOJO.getName(),
                        routingOrderPOJO.getTimeScheduled(),
                        routingOrderPOJO.getItemPOJOPK(),
                        routingOrderPOJO.getMessage(),
                        routingOrderPOJO.getServiceJNDI(),
                        routingOrderPOJO.getServiceParameters(),
                        routingOrderPOJO.getBindingUniverseName(),
                        routingOrderPOJO.getBindingUserToken()
                );
                //delete the existing one
                if (cleanUpRoutingOrder) {
                    removeRoutingOrder(routingOrderPOJO.getAbstractRoutingOrderPOJOPK());
                }
                //switch variables
                routingOrderPOJO = activeRO;
                break;
        }
        //update timing flags
        routingOrderPOJO.setTimeLastRunStarted(System.currentTimeMillis());
        //Now anything goes right or wrong, we clean up the routing order from the active queue
        Object service = null;
        try {
            service = Util.retrieveComponent(routingOrderPOJO.getServiceJNDI());
        } catch (Exception e) {
            String err = "Unable to execute the Routing Order '" + routingOrderPOJO.getName() + "'." +
                    " The service: '" + routingOrderPOJO.getServiceJNDI() + "' is not found. " + e.getMessage();
            moveToFailedQueue(routingOrderPOJO, err, e, true);
        }
        String result = null;
        try {
            if (Util.getMethod(service, "setRoutingOrderPOJO") != null) {
                Util.getMethod(service, "setRoutingOrderPOJO").invoke(service, routingOrderPOJO);
            }
            result = (String) Util.getMethod(service, "receiveFromInbound").invoke(
                    service,
                    routingOrderPOJO.getItemPOJOPK(),
                    routingOrderPOJO.getName(),
                    routingOrderPOJO.getServiceParameters());
        } catch (IllegalArgumentException e) {
            String err = "Unable to execute the Routing Order '" + routingOrderPOJO.getName() + "'." +
                    " The service: '" + routingOrderPOJO.getServiceJNDI() + "' cannot be executed due to wrong parameters. " + e.getMessage();
            moveToFailedQueue(routingOrderPOJO, err, e, true);
        } catch (IllegalAccessException e) {
            String err = "Unable to execute the Routing Order '" + routingOrderPOJO.getName() + "'." +
                    " The service: '" + routingOrderPOJO.getServiceJNDI() + "' cannot be executed due to security reasons. " + e.getMessage();
            moveToFailedQueue(routingOrderPOJO, err, e, true);
            throw new XtentisException(err);
        } catch (InvocationTargetException e) {
            String err = "Unable to execute the Routing Order '" + routingOrderPOJO.getName() + "'." +
                    " The service: '" + routingOrderPOJO.getServiceJNDI() + "' failed. ";
            if (e.getCause() != null) {
                err += (e.getCause() instanceof XtentisException ? "" : e.getCause().getClass().getName() + ": ") + e.getCause().getMessage();
            }
            moveToFailedQueue(routingOrderPOJO, err, e, true);
        }
        //The service call completed successfully -- add to the COMPLETED queue
        moveToCompletedQueue(
                routingOrderPOJO,
                null,
                true
        );
        return result;
    }

    private void moveToFailedQueue(AbstractRoutingOrderV2POJO routingOrderPOJO, String message, Exception e, boolean cleanUpRoutingOrder) throws XtentisException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("addToErrorQueue() " + routingOrderPOJO.getName() + ": " + message);
        }
        //Log
        if (LOGGING_EVENT.equals(routingOrderPOJO.getItemPOJOPK().getConceptName())) {
            LOGGER.info("ERROR SYSTRACE: " + message, e);
        } else {
            LOGGER.error(message, e);
        }
        //Create Failed Routing Order
        FailedRoutingOrderV2POJO failedRO;
        if (routingOrderPOJO instanceof FailedRoutingOrderV2POJO) {
            failedRO = (FailedRoutingOrderV2POJO) routingOrderPOJO;
        } else {
            failedRO = new FailedRoutingOrderV2POJO(routingOrderPOJO);
            if (cleanUpRoutingOrder) {
                removeRoutingOrder(routingOrderPOJO.getAbstractRoutingOrderPOJOPK());
            }
        }
        failedRO.setMessage(
                failedRO.getMessage() +
                        "\n---> FAILED " + sdf.format(new Date()) + ": " + message
        );
        putRoutingOrder(failedRO);
        throw new RoutingException(message);
    }

    private void moveToCompletedQueue(AbstractRoutingOrderV2POJO routingOrderPOJO, String message, boolean cleanUpRoutingOrder) throws XtentisException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("moveToCompletedQueue() " + routingOrderPOJO.getName() + ": " + message);
        }
        try {
            //Create the Completed Routing Order
            CompletedRoutingOrderV2POJO completedRO;
            if (routingOrderPOJO instanceof CompletedRoutingOrderV2POJO) {
                completedRO = (CompletedRoutingOrderV2POJO) routingOrderPOJO;
            } else {
                completedRO = new CompletedRoutingOrderV2POJO(routingOrderPOJO);
                if (cleanUpRoutingOrder) {
                    removeRoutingOrder(routingOrderPOJO.getAbstractRoutingOrderPOJOPK());
                }
            }
            completedRO.setMessage(
                    completedRO.getMessage() +
                            "\n---> COMPLETED " + sdf.format(new Date()) + (message != null ? "\n" + message : "")
            );
            putRoutingOrder(completedRO);
        } catch (XtentisException e) {
            //try to move to the error queue
            String err = "ERROR SYSTRACE: Moving of routing order '" + routingOrderPOJO.getName() + "' to COMPLETED queue with message  '" + message + "' failed. Moving to FAILED queue.";
            LOGGER.info(err, e);
        }
    }

    /**
     * Executes a Routing Order in delay milliseconds
     */
    public void executeAsynchronously(AbstractRoutingOrderV2POJO routingOrderPOJO, long delayInMillis) throws XtentisException {
        // TODO
        // createTimer(routingOrderPOJO, delayInMillis);
    }

    /**
     * Executes a Routing Order in default DELAY milliseconds
     */
    public void executeAsynchronously(AbstractRoutingOrderV2POJO routingOrderPOJO) throws XtentisException {
        // TODO
        // createTimer(routingOrderPOJO, DELAY);
    }

    /**
     * Remove an item
     */
    public AbstractRoutingOrderV2POJOPK removeRoutingOrder(AbstractRoutingOrderV2POJOPK pk)
            throws XtentisException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("removeRoutingOrder() " + pk.getUniqueId());
        }
        try {
            if (ObjectPOJO.load(pk.getRoutingOrderClass(), pk) != null) {
                ObjectPOJO.remove(pk.getRoutingOrderClass(), pk);
            }
            return pk;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the Routing Order of class " + pk.getRoutingOrderClass() + " and id " + pk.getName()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Creates or updates a Transformer
     */
    public AbstractRoutingOrderV2POJOPK putRoutingOrder(AbstractRoutingOrderV2POJO routingOrderPOJO) throws XtentisException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("putRouting Order() " + routingOrderPOJO.getName());
        }
        try {
            ObjectPOJOPK pk = routingOrderPOJO.store();
            if (pk == null) {
                return null;
            }
            return routingOrderPOJO.getAbstractRoutingOrderPOJOPK();
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to create/update the Routing Order. " + routingOrderPOJO.getPK().getUniqueId() + " with message\n" + routingOrderPOJO.getMessage()
                    + "\n Exception: " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get Routing Order
     */
    public AbstractRoutingOrderV2POJO getRoutingOrder(AbstractRoutingOrderV2POJOPK pk) throws XtentisException {
        try {
            AbstractRoutingOrderV2POJO routingOrder = ObjectPOJO.load(pk.getRoutingOrderClass(), pk);
            if (routingOrder == null) {
                String err = "The Routing Order " + pk.getUniqueId() + " does not exist.";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            return routingOrder;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the Routing Order of class " + pk.getRoutingOrderClass() + " and id " + pk.getName()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get a RoutingOrder knowing its class - no exception is thrown: returns null if not found
     */
    public AbstractRoutingOrderV2POJO existsRoutingOrder(AbstractRoutingOrderV2POJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(pk.getRoutingOrderClass(), pk);
        } catch (XtentisException e) {
            return null;
        } catch (Exception e) {
            String info = "Unable to check the existence of the Routing Order of class " + pk.getRoutingOrderClass() + " and id " + pk.getName()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("existsRoutingOrder() " + info, e);
            }
            return null;
        }
    }

    /**
     * Find Active Routing Orders
     */
    public ActiveRoutingOrderV2POJO[] findActiveRoutingOrders(long lastScheduledTime, int maxRoutingOrders) throws XtentisException {
        Collection<AbstractRoutingOrderV2POJOPK> pks = getRoutingOrderPKsByCriteriaWithPaging(ActiveRoutingOrderV2POJO.class,
                null,
                null,
                -1,
                -1,
                -1,
                lastScheduledTime,
                -1,
                -1,
                -1,
                -1,
                null,
                null,
                null,
                null,
                null,
                0,
                maxRoutingOrders,
                false);
        if (pks.isEmpty()) {
            return new ActiveRoutingOrderV2POJO[0];
        }
        ArrayList<ActiveRoutingOrderV2POJO> routingOrdersList = new ArrayList<ActiveRoutingOrderV2POJO>();
        for (AbstractRoutingOrderV2POJOPK pk : pks) {
            ActiveRoutingOrderV2POJO routingOrder = ObjectPOJO.load(ActiveRoutingOrderV2POJO.class, pk);
            routingOrdersList.add(routingOrder);
        }
        return routingOrdersList.toArray(new ActiveRoutingOrderV2POJO[routingOrdersList.size()]);
    }

    /**
     * Find Dead Routing Orders
     */
    public ActiveRoutingOrderV2POJO[] findDeadRoutingOrders(long maxLastRunStartedTime, int maxRoutingOrders) throws XtentisException {
        Collection<AbstractRoutingOrderV2POJOPK> pks = getRoutingOrderPKsByCriteriaWithPaging(ActiveRoutingOrderV2POJO.class,
                null,
                null,
                -1,
                -1,
                -1,
                -1,
                maxLastRunStartedTime,
                -1,
                -1,
                -1,
                null,
                null,
                null,
                null,
                null,
                0,
                maxRoutingOrders,
                false);
        if (pks.isEmpty()) {
            return new ActiveRoutingOrderV2POJO[0];
        }
        ArrayList<ActiveRoutingOrderV2POJO> routingOrdersList = new ArrayList<ActiveRoutingOrderV2POJO>();
        for (AbstractRoutingOrderV2POJOPK pk : pks) {
            ActiveRoutingOrderV2POJO routingOrder = ObjectPOJO.load(ActiveRoutingOrderV2POJO.class, pk);
            routingOrdersList.add(routingOrder);
        }
        return routingOrdersList.toArray( new ActiveRoutingOrderV2POJO[routingOrdersList.size()]);
    }

    /**
     * Retrieve all Active Routing Order PKs
     */
    public Collection<ActiveRoutingOrderV2POJOPK> getActiveRoutingOrderPKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> c = ObjectPOJO.findAllPKs(ActiveRoutingOrderV2POJO.class, regex);
        ArrayList<ActiveRoutingOrderV2POJOPK> l = new ArrayList<ActiveRoutingOrderV2POJOPK>();
        for (ObjectPOJOPK activeRoutingOrder : c) {
            l.add(new ActiveRoutingOrderV2POJOPK(activeRoutingOrder));
        }
        return l;
    }

    /**
     * Retrieve all Completed Routing Order PKs
     */
    public Collection<CompletedRoutingOrderV2POJOPK> getCompletedRoutingOrderPKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> c = ObjectPOJO.findAllPKs(CompletedRoutingOrderV2POJO.class, regex);
        ArrayList<CompletedRoutingOrderV2POJOPK> l = new ArrayList<CompletedRoutingOrderV2POJOPK>();
        for (ObjectPOJOPK completedRoutingOrder : c) {
            l.add(new CompletedRoutingOrderV2POJOPK(completedRoutingOrder));
        }
        return l;
    }

    /**
     * Retrieve all Failed Routing Order PKs
     */
    public Collection<FailedRoutingOrderV2POJOPK> getFailedRoutingOrderPKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> c = ObjectPOJO.findAllPKs(FailedRoutingOrderV2POJO.class, regex);
        ArrayList<FailedRoutingOrderV2POJOPK> l = new ArrayList<FailedRoutingOrderV2POJOPK>();
        for (ObjectPOJOPK failedRoutingOrder : c) {
            l.add(new FailedRoutingOrderV2POJOPK(failedRoutingOrder));
        }
        return l;
    }


    /**
     * Retrieve all RoutingOrder PKs whatever the class
     */
    public Collection<AbstractRoutingOrderV2POJOPK> getAllRoutingOrderPKs(String regex) throws XtentisException {
        ArrayList<AbstractRoutingOrderV2POJOPK> l = new ArrayList<AbstractRoutingOrderV2POJOPK>();
        l.addAll(getActiveRoutingOrderPKs(regex));
        l.addAll(getCompletedRoutingOrderPKs(regex));
        l.addAll(getFailedRoutingOrderPKs(regex));
        return l;

    }

    /**
     * Retrieve all RoutingOrder PKs by CriteriaWithPaging
     */
    public Collection<AbstractRoutingOrderV2POJOPK> getRoutingOrderPKsByCriteriaWithPaging(
            Class<? extends AbstractRoutingOrderV2POJO> routingOrderV2POJOClass, String anyFieldContains, String name,
            long timeCreatedMin, long timeCreatedMax, long timeScheduledMin, long timeScheduledMax, long timeLastRunStartedMin,
            long timeLastRunStartedMax, long timeLastRunCompletedMin, long timeLastRunCompletedMax, String itemConceptContains,
            String itemIDsContain, String serviceJNDIContains, String serviceParametersContains, String messageContains,
            int start, int limit, boolean withTotalCount) throws XtentisException {

        String pojoName = ObjectPOJO.getObjectRootElementName(ObjectPOJO.getObjectName(routingOrderV2POJOClass));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "getRoutingOrderPKsByCriteriaWithPaging() " + routingOrderV2POJOClass + "  " //$NON-NLS-1$ //$NON-NLS-2$
                            + ObjectPOJO.getObjectName(routingOrderV2POJOClass) + "  " + pojoName); //$NON-NLS-1$
        }

        WhereAnd wAnd = new WhereAnd();

        if ((anyFieldContains != null) && (!"*".equals(anyFieldContains))) { //$NON-NLS-1$
            wAnd.add(new WhereCondition(pojoName + "//*", WhereCondition.CONTAINS, anyFieldContains, WhereCondition.PRE_NONE, //$NON-NLS-1$
                    false));
        }
        if ((name != null) && (!"*".equals(name))) { //$NON-NLS-1$
            wAnd.add(new WhereCondition(pojoName + "/name", WhereCondition.CONTAINS, name, WhereCondition.PRE_NONE, false)); //$NON-NLS-1$
        }
        if (timeCreatedMin > 0) {
            wAnd.add(new WhereCondition(pojoName + "/@time-created", WhereCondition.GREATER_THAN_OR_EQUAL, "" + timeCreatedMin, //$NON-NLS-1$ //$NON-NLS-2$
                    WhereCondition.PRE_NONE, false));
        }
        if (timeCreatedMax > 0) {
            wAnd.add(new WhereCondition(pojoName + "/@time-created", WhereCondition.LOWER_THAN_OR_EQUAL, "" + timeCreatedMax, //$NON-NLS-1$ //$NON-NLS-2$
                    WhereCondition.PRE_NONE, false));
        }
        if (timeScheduledMin > 0) {
            wAnd.add(new WhereCondition(pojoName + "/@time-scheduled", WhereCondition.GREATER_THAN_OR_EQUAL, "" //$NON-NLS-1$//$NON-NLS-2$
                    + timeScheduledMin, WhereCondition.PRE_NONE, false));
        }
        if (timeScheduledMax > 0) {
            wAnd.add(new WhereCondition(pojoName + "/@time-scheduled", WhereCondition.LOWER_THAN_OR_EQUAL, "" + timeScheduledMax, //$NON-NLS-1$ //$NON-NLS-2$
                    WhereCondition.PRE_NONE, false));
        }
        if (timeLastRunStartedMin > -2) {
            wAnd.add(new WhereCondition(pojoName + "/@time-last-run-started", WhereCondition.GREATER_THAN_OR_EQUAL, "" //$NON-NLS-1$//$NON-NLS-2$
                    + timeLastRunStartedMin, WhereCondition.PRE_NONE, false));
        }
        if (timeLastRunStartedMax > 0) {
            wAnd.add(new WhereCondition(pojoName + "/@time-last-run-started", WhereCondition.LOWER_THAN_OR_EQUAL, "" //$NON-NLS-1$//$NON-NLS-2$
                    + timeLastRunStartedMax, WhereCondition.PRE_NONE, false));
        }
        if (timeLastRunCompletedMin > 0) {
            wAnd.add(new WhereCondition(pojoName + "/@time-last-run-completed", WhereCondition.GREATER_THAN_OR_EQUAL, "" //$NON-NLS-1$//$NON-NLS-2$
                    + timeLastRunCompletedMin, WhereCondition.PRE_NONE, false));
        }
        if (timeLastRunCompletedMax > 0) {
            wAnd.add(new WhereCondition(pojoName + "/@time-last-run-completed", WhereCondition.LOWER_THAN_OR_EQUAL, "" //$NON-NLS-1$ //$NON-NLS-2$
                    + timeLastRunCompletedMax, WhereCondition.PRE_NONE, false));
        }
        if ((itemConceptContains != null) && (!"*".equals(itemConceptContains))) { //$NON-NLS-1$
            wAnd.add(new WhereCondition(pojoName + "/item-pOJOPK/concept-name", WhereCondition.CONTAINS, itemConceptContains, //$NON-NLS-1$
                    WhereCondition.PRE_AND, false));
        }
        if ((itemIDsContain != null) && (!"*".equals(itemIDsContain))) { //$NON-NLS-1$
            wAnd.add(new WhereCondition(pojoName + "/item-pOJOPK/ids", WhereCondition.CONTAINS, itemIDsContain, //$NON-NLS-1$
                    WhereCondition.PRE_AND, false));
        }
        if ((serviceJNDIContains != null) && (!"*".equals(serviceJNDIContains))) { //$NON-NLS-1$
            wAnd.add(new WhereCondition(pojoName + "/service-jNDI", WhereCondition.CONTAINS, serviceJNDIContains.contains("/") //$NON-NLS-1$ //$NON-NLS-2$
                    || serviceJNDIContains.startsWith("*") ? serviceJNDIContains : serviceJNDIContains, WhereCondition.PRE_AND, //$NON-NLS-1$
                    false));
        }
        if ((serviceParametersContains != null) && (!"*".equals(serviceParametersContains))) { //$NON-NLS-1$
            wAnd.add(new WhereCondition(pojoName + "/service-parameters", WhereCondition.CONTAINS, serviceParametersContains, //$NON-NLS-1$
                    WhereCondition.PRE_AND, false));
        }
        if ((messageContains != null) && (!"*".equals(messageContains))) { //$NON-NLS-1$
            wAnd.add(new WhereCondition(pojoName + "/message", WhereCondition.CONTAINS, messageContains, WhereCondition.PRE_AND, //$NON-NLS-1$
                    false));
        }
        ArrayList<AbstractRoutingOrderV2POJOPK> list = new ArrayList<AbstractRoutingOrderV2POJOPK>();
        Collection<ObjectPOJOPK> col = ObjectPOJO.findPKsByCriteriaWithPaging(routingOrderV2POJOClass, new String[]{
                pojoName + "/name", pojoName + "/@status"}, wAnd.getSize() == 0 ? null : wAnd, null, null, start, limit, withTotalCount);//$NON-NLS-1$ //$NON-NLS-2$
        for (ObjectPOJOPK objectPOJOPK : col) {
            if (routingOrderV2POJOClass.equals(ActiveRoutingOrderV2POJO.class)) {
                list.add(new ActiveRoutingOrderV2POJOPK(objectPOJOPK.getIds()[0]));
            } else if (routingOrderV2POJOClass.equals(CompletedRoutingOrderV2POJO.class)) {
                list.add(new CompletedRoutingOrderV2POJOPK(objectPOJOPK.getIds()[0]));
            } else if (routingOrderV2POJOClass.equals(FailedRoutingOrderV2POJO.class)) {
                list.add(new FailedRoutingOrderV2POJOPK(objectPOJOPK.getIds()[0]));
            }
        }
        return list;
    }

    /**
     * Retrieve all RoutingOrder PKs by Criteria
     */
    public Collection<AbstractRoutingOrderV2POJOPK> getRoutingOrderPKsByCriteria(
            Class<? extends AbstractRoutingOrderV2POJO> routingOrderV2POJOClass,
            String anyFieldContains,
            String name,
            long timeCreatedMin, long timeCreatedMax,
            long timeScheduledMin, long timeScheduledMax,
            long timeLastRunStartedMin, long timeLastRunStartedMax,
            long timeLastRunCompletedMin, long timeLastRunCompletedMax,
            String itemConceptContains,
            String itemIDsContain,
            String serviceJNDIContains,
            String serviceParametersContains,
            String messageContains) throws XtentisException {
        return getRoutingOrderPKsByCriteriaWithPaging(routingOrderV2POJOClass, anyFieldContains, name, timeCreatedMin,
                timeCreatedMax, timeScheduledMin, timeScheduledMax, timeLastRunStartedMin, timeLastRunStartedMax,
                timeLastRunCompletedMin, timeLastRunCompletedMax, itemConceptContains, itemIDsContain, serviceJNDIContains,
                serviceParametersContains, messageContains, 0, Integer.MAX_VALUE, false);
    }
}