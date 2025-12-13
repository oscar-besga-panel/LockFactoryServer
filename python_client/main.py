import time
import grpc
from google.protobuf import wrappers_pb2
from LockFactoryServerGrpc_pb2_grpc import LockServerStub
from LockFactoryServerGrpc_pb2 import NameTokenValues, LockStatus

def current_milli_time():
    return round(time.time() * 1000)
def list_methods(obj):
    return [method_name for method_name in dir(obj)
            if callable(getattr(obj, method_name))]

# def instrument_methods(obj):
    # Instrospecting the result object
    # print("Lock Status Response received:", result.lockStatus)
    # print(type(result))
    # print("Methods of the result object:", list_methods(result))
    # print(type(result.lockStatus))
    # print("Lock Status LockFactoryServerGrpc_pb2.LockStatusValues")
    # print(type(LockFactoryServerGrpc_pb2.LockStatusValues))
    # print(LockFactoryServerGrpc_pb2.LockStatusValues.__dict__)
    # print("Methods of the result object:", list_methods(LockFactoryServerGrpc_pb2.LockStatusValues))
    # print("Lock Status LockFactoryServerGrpc_pb2.LockStatus")
    # print(LockFactoryServerGrpc_pb2.LockStatus.__dict__)
    # print("Methods of the result object:", list_methods(LockFactoryServerGrpc_pb2.LockStatus))

def run():

    print("Hello Lock!")
    channel = grpc.insecure_channel('localhost:50051')
    stub = LockServerStub(channel)
    lockName = "pyTestLock_" + str(current_milli_time())
    print("Lock Name:", lockName)
    lockToken = stub.lock(wrappers_pb2.StringValue(value = lockName))
    lockToken = lockToken.value
    print("Lock Token:", lockToken)
    nameTokenValue = NameTokenValues(name=lockName, token=lockToken)

    result = stub.lockStatus(nameTokenValue)

    print("Lock Status Enum Value:", LockStatus.Name(result.lockStatus))

    unLockResponse = stub.unLock(nameTokenValue)
    unLockResponse = unLockResponse.value
    print("Unlock Response received:", unLockResponse)

    channel.close()

if __name__ == "__main__":
    run()
