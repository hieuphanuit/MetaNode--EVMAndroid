




// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
#include <jni.h>
#include <syslog.h>
#include <string>
#include "eEVM/opcode.h"
#include "eEVM/processor.h"
#include "eEVM/simple/simpleglobalstate.h"
#include "3rdparty/nlohmann/json.hpp"

#include <cassert>
#include <fmt/format_header_only.h>
#include <fstream>
#include <iostream>
#include <nlohmann/json.hpp>
#include <random>
#include <sstream>
#include <vector>

struct Environment
{
  eevm::GlobalState& gs;
  const eevm::Address& owner_address;
  const uint256_t& owner_last_hash;
  std::vector<uint8_t> code;
};

// Run input as an EVM transaction, check the result and return the output
eevm::ExecResult run(
  Environment& env,
  const eevm::Address& from,
  const eevm::Address& to,
  const eevm::Code& input,
  // << MYCODE
  JNIEnv* jenv,
  jobject obj
  //>>
  )
{
  // Ignore any logs produced by this transaction
  eevm::NullLogHandler ignore;
  eevm::Transaction tx(from, ignore);

  // Record a trace to aid debugging
  eevm::Trace tr;
  eevm::Processor p(env.gs);

  // Run the transaction
  const auto exec_result = p.run(tx, from, env.gs.get(to), input, 0u, jenv, obj, &tr);
  return exec_result;
}

char* get_hex(uint8_t *s, size_t len) {
    char * rs = new char[len * 2];
    for(size_t i = 0; i < len; i++) {
        sprintf(rs + i * 2 ,"%02x", s[i]);
    }
    syslog(LOG_DEBUG, "get hex len %d",strlen(rs));
    return rs;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_example_nativec_MainActivity_deploy(
        JNIEnv* jenv,
        jobject jobj,
        jbyteArray jb_caller_address,
        jbyteArray jb_caller_last_hash,
        jbyteArray jb_contract_balance,
        jbyteArray jb_contract_constructor
) {
    char* b_caller_last_hash = (char*)jenv->GetByteArrayElements(jb_caller_last_hash, NULL);
    char* b_caller_address = (char*)jenv->GetByteArrayElements(jb_caller_address, NULL);
    char* b_contract_balance = (char*)jenv->GetByteArrayElements(jb_contract_balance, NULL);
    char* b_contract_constructor = (char*)jenv->GetByteArrayElements(jb_contract_constructor, NULL);
    uint contract_constructor_length = jenv->GetArrayLength(jb_contract_constructor);
   // format argument to right data type
    uint256_t caller_address = eevm::from_big_endian((uint8_t *)b_caller_address, 20u);
    uint256_t caller_last_hash = eevm::from_big_endian((uint8_t *)b_caller_last_hash, 32u);
    uint256_t contract_balance = eevm::from_big_endian((uint8_t *)b_contract_balance, 32u);
    std::vector<uint8_t> contract_constructor((uint8_t*)b_contract_constructor, (uint8_t*)b_contract_constructor + contract_constructor_length);

    eevm::SimpleGlobalState gs;
    //  init env
    Environment env{
      gs, 
      caller_address,
      caller_last_hash, 
      {}
    };

    // contract_constructor include contract code and contructor argument in it
    const auto contract_address = eevm::generate_contract_address(env.owner_address, env.owner_last_hash);

    // TODO: may check if account have any data ?? or just skip and let node do it?
    std::cout << contract_balance << std::endl;
    // Set this constructor as the contract's code body
    auto contract = env.gs.create(contract_address, 0u, contract_constructor);

    // Run a transaction to initialise this account
    auto result =
        run(env, env.owner_address, contract_address, {}, jenv, jobj);

    // Result of running the compiled constructor is the code that should be the
    // contract's body (constructor will also have setup contract's Storage)
    contract.acc.set_code(std::move(result.output));
    std::vector<uint8_t> code = contract.acc.get_code();

    uint8_t b_contract_address[20u] = {};
    std::memcpy(b_contract_address, &contract_address, 20u);

    jclass clazz = jenv->FindClass("com/example/nativec/EVMDeployRs");

    // exitReason and exception and exmsg
    jbyte jb_exitReason = (jbyte)result.er;
    jbyte jb_exception = (jbyte)result.ex;
    jbyteArray jb_exmsg = jenv->NewByteArray(result.exmsg.size());
    jenv->SetByteArrayRegion( jb_exmsg, 0, result.exmsg.size(), (const jbyte*)result.exmsg.c_str() );

    // address and code
    jbyteArray jb_deployed_address = jenv->NewByteArray(20);
    jbyteArray jb_deployed_code = jenv->NewByteArray(code.size());
    jenv->SetByteArrayRegion( jb_deployed_address, 0, 20u, (const jbyte*)&b_contract_address );
    jenv->SetByteArrayRegion( jb_deployed_code, 0, code.size(), (const jbyte*)code.data() );
    // storage
    nlohmann::json json_storage = env.gs.get(contract_address).st.get_storage();
    std::string str_storage = json_storage.dump();
    jbyteArray jb_storage = jenv->NewByteArray(str_storage.size());
    jenv->SetByteArrayRegion( jb_storage, 0, str_storage.size(), (const jbyte*)str_storage.c_str() );

    // Get the method id of an empty constructor in clazz
    jmethodID constructor = jenv->GetMethodID(clazz, "<init>", "(BB[B[B[B[B)V");
    // Create an instance of clazz
    jobject obj = jenv->NewObject(
            clazz,
            constructor,
            jb_exitReason,
            jb_exception,
            jb_exmsg,
            jb_deployed_address,
            jb_deployed_code,
            jb_storage
    );

    syslog(LOG_DEBUG, "contract address %s", get_hex((uint8_t *)b_contract_address, 20u));
    syslog(LOG_DEBUG, "Exit reason %d", jb_exitReason);
//
    jenv->ReleaseByteArrayElements(jb_caller_address, (jbyte*)b_caller_address, JNI_ABORT);
    jenv->ReleaseByteArrayElements(jb_caller_last_hash, (jbyte*)b_caller_last_hash, JNI_ABORT);
    jenv->ReleaseByteArrayElements(jb_contract_balance, (jbyte*)b_contract_balance, JNI_ABORT);
    jenv->ReleaseByteArrayElements(jb_contract_constructor, (jbyte*)b_contract_constructor, JNI_ABORT);

    return obj;
}

eevm::AccountState init_contract_with_storage(
  Environment& env, 
  eevm::Address contract_address, 
  uint256_t balance, 
  eevm::Code& code, 
  const nlohmann::json& j_storage
) {
  return env.gs.create_with_storage(contract_address, balance, code, j_storage);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_example_nativec_MainActivity_call(
        JNIEnv* jenv,
        jobject jobj,
        jbyteArray jb_caller_address,
        jbyteArray jb_caller_last_hash,
        jbyteArray jb_contract_address,
        jbyteArray jb_contract_balance,
        jstring jstr_old_storage,
        jbyteArray jb_code,
        jbyteArray jb_input
) {
    char* b_caller_address = (char*)jenv->GetByteArrayElements(jb_caller_address, NULL);
    char* b_caller_last_hash = (char*)jenv->GetByteArrayElements(jb_caller_last_hash, NULL);
    char* b_contract_address = (char*)jenv->GetByteArrayElements(jb_contract_address, NULL);
    char* b_contract_balance = (char*)jenv->GetByteArrayElements(jb_contract_balance, NULL);
    char* old_storage = (char*)jenv->GetStringUTFChars(jstr_old_storage, NULL);
    char* b_code = (char*)jenv->GetByteArrayElements(jb_code, NULL);
    uint code_length = jenv->GetArrayLength(jb_code);
    char* b_input = (char*)jenv->GetByteArrayElements(jb_input, NULL);
    uint input_length = jenv->GetArrayLength(jb_input);

    // format argument to right data type
    uint256_t caller_address = eevm::from_big_endian((uint8_t *)b_caller_address, 20u);
    uint256_t caller_last_hash = eevm::from_big_endian((uint8_t *)b_caller_last_hash, 32u);
    uint256_t contract_address = eevm::from_big_endian((uint8_t *)b_contract_address, 20u);
    uint256_t contract_balance = eevm::from_big_endian((uint8_t *)b_contract_balance, 32u);
    std::vector<uint8_t> code((uint8_t*)b_code, (uint8_t*)b_code + code_length);
    std::vector<uint8_t> input((uint8_t*)b_input, (uint8_t*)b_input + input_length);
    nlohmann::json j_storage = nlohmann::json::parse((std::string)old_storage);

    eevm::SimpleGlobalState gs;
    //  init env
    Environment env{
            gs,
            caller_address,
            caller_last_hash,
            {}
    };
    // init contract and contract storage
    init_contract_with_storage(
            env,
            contract_address,
            contract_balance,
            code,
            j_storage
    );
    const auto result =
            run(env, caller_address, contract_address, input, jenv, jobj);

    // exitReason and exception and exmsg
    jbyte jb_exitReason = (jbyte)result.er;
    jbyte jb_exception = (jbyte)result.ex;
    jbyteArray jb_exmsg = jenv->NewByteArray(result.exmsg.size());
    jenv->SetByteArrayRegion( jb_exmsg, 0, result.exmsg.size(), (const jbyte*)result.exmsg.c_str() );

    // storage
    nlohmann::json json_storage = env.gs.get(contract_address).st.get_storage();
    std::string str_storage = json_storage.dump();
    jbyteArray jb_storage = jenv->NewByteArray(str_storage.size());
    jenv->SetByteArrayRegion( jb_storage, 0, str_storage.size(), (const jbyte*)str_storage.c_str() );
    // output
    jbyteArray jb_output = jenv->NewByteArray(result.output.size());
    jenv->SetByteArrayRegion( jb_output, 0, result.output.size(), (const jbyte*)result.output.data() );

    jclass clazz = jenv->FindClass("com/example/nativec/EVMCallRs");
    jmethodID constructor = jenv->GetMethodID(clazz, "<init>", "(BB[B[B[B)V");
    // Create an instance of clazz
    jobject obj = jenv->NewObject(
            clazz,
            constructor,
            jb_exitReason,
            jb_exception,
            jb_exmsg,
            jb_output,
            jb_storage
    );

    jenv->ReleaseByteArrayElements(jb_caller_address, (jbyte*)b_caller_address, JNI_ABORT);
    jenv->ReleaseByteArrayElements(jb_caller_last_hash, (jbyte*)b_caller_last_hash, JNI_ABORT);
    jenv->ReleaseByteArrayElements(jb_contract_address, (jbyte*)b_contract_address, JNI_ABORT);
    jenv->ReleaseByteArrayElements(jb_contract_balance, (jbyte*)b_contract_balance, JNI_ABORT);
    jenv->ReleaseStringUTFChars(jstr_old_storage, old_storage);
    jenv->ReleaseByteArrayElements(jb_code, (jbyte*)b_code, JNI_ABORT);

    syslog(LOG_DEBUG, "Exit reason %d", jb_exitReason);
    return obj;
}



void append_argument(std::vector<uint8_t>& code, const uint256_t& arg)
{
  // To ABI encode a function call with a uint256_t (or Address) argument,
  // simply append the big-endian byte representation to the code (function
  // selector, or bin). ABI-encoding for more complicated types is more
  // complicated, so not shown in this sample.
  const auto pre_size = code.size();
  code.resize(pre_size + 32u);
  eevm::to_big_endian(arg, code.data() + pre_size);
}