package com.smartcontract.template

import com.alibaba.fastjson.JSONObject

static def sum(a, b) {
    return a + b
}

static def invoiceAmountChange(externalParams, internalParams) {
    def instructionPipeline = []
    def invoiceLeg = internalParams.legs.find { it.type.equals('invoiceLeg') }
    def strPayBatch = ''
    if(invoiceLeg != null) {
        def idx = internalParams.variables.settlementBatches.findIndexOf({it -> it.batchNo.equals(externalParams.settlementBatchNo)})
        if(idx >= 0) {
            if (externalParams.payerId.equals(invoiceLeg.payer)) {
                internalParams.variables.settlementBatches[idx].invoiceWeight += externalParams.invoiceWeight
                internalParams.variables.settlementBatches[idx].invoiceAmount += externalParams.invoiceAmount
            } else {
                internalParams.variables.settlementBatches[idx].invoiceWeight -= externalParams.invoiceWeight
                internalParams.variables.settlementBatches[idx].invoiceAmount -= externalParams.invoiceAmount
            }
            if(internalParams.variables.settlementBatches[idx].isSell == 0) { // only purchase contract has invoice subject
                def idxPayInvoice = internalParams.variables.paymentBatches.findIndexOf({it -> (it.settlementBatchNo.equals(externalParams.settlementBatchNo) && it.paymentType == 1)})
                strPayBatch = internalParams.variables.paymentBatches[idxPayInvoice].batchNo
                internalParams.variables.paymentBatches[idxPayInvoice].weightExpected += externalParams.invoiceWeight
                def newExpCash = externalParams.invoiceWeight * internalParams.variables.settlementBatches[idx].settlementPrice * internalParams.paymentInfo.ratioPaymentAfterInvoice / 100
                internalParams.variables.paymentBatches[idxPayInvoice].cashExpected = (internalParams.variables.paymentBatches[idxPayInvoice].cashExpected + newExpCash).round(2)
                internalParams.variables.settlementBatches[idx].expectedCashAfterInvoice = internalParams.variables.paymentBatches[idxPayInvoice].cashExpected
            }
        }
    }
    if(instructionPipeline.indexOf('invoiceAcknowledgmentInstruction') < 0) {
        instructionPipeline.add('invoiceAcknowledgmentInstruction')
    }
    def dslResult = [
            eventNo: externalParams.eventNo,
            settlementBatchNo: externalParams.settlementBatchNo,
            paymentBatchNo: strPayBatch,
            tradeId: externalParams.tradeId,
            dealId: externalParams.dealId,
            status: sum(0,1)
    ]
    def output = new JSONObject()
    output.put("instructionPipeline", instructionPipeline)
    output.put("instructionResult", dslResult)
    output.put("variables", internalParams.variables)
    return output
}
