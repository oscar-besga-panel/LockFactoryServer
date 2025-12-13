
With python 3.9

pip install -r  .\requeriments.txt


python -m grpc_tools.protoc -I../core/src/main/proto --python_out=./ --grpc_python_out=./ ../core/src/main/proto/LockFactoryServerGrpc.proto
