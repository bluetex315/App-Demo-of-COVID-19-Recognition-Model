import torch
from PIL import Image
from torchvision import transforms
from torch.utils.mobile_optimizer import optimize_for_mobile

from models import resnet
from models import ffn as fn

def getmodels(path, isresnet):

    if isresnet:
        model = resnet.resnet34(num_classes=2)
        model = model.cuda()
    else:
        model = fn.ffn()
        model = model.cuda()

    checkpoint = torch.load(path, map_location="cuda:0")
    model.load_state_dict(checkpoint['state_dict'])
    model.eval()

    return model

def convertion(model, result_path):

    scripted_module = torch.jit.script(model)

    print(scripted_module.graph)
    # model optimization for inference
    optimized_scripted_module = optimize_for_mobile(scripted_module)

    # Export full jit version model (not compatible with lite interpreter)
    # scripted_module.save(result_path)

    # Export lite interpreter version model (compatible with lite interpreter)
    # scripted_module._save_for_lite_interpreter(result_path)

    # using optimized lite interpreter model makes inference about 60% faster than the non-optimized lite interpreter model, which is about 6% faster than the non-optimized full jit model
    optimized_scripted_module._save_for_lite_interpreter(result_path)

    print("finished")

def main():

    # model path
    B_PTH_PATH = "./checkpoint/best_models/breathing_1.pth"
    C_PTH_PATH = "./checkpoint/best_models/cough_1.pth"
    S_PTH_PATH = "./checkpoint/best_models/speech_1.pth"
    FFN_PTH_PATH = "./checkpoint/best_models/ffn_2.pth"

    # jit output path
    B_JIT_PATH = "./models_ptl/breathing_optimized.ptl"
    C_JIT_PATH = "./models_ptl/cough_optimized.ptl"
    S_JIT_PATH = "./models_ptl/speech_optimized.ptl"
    FFN_JIT_PATH = "./models_ptl/ffn_optimized.ptl"

    # initialize model
    model_b = getmodels(B_PTH_PATH, True)
    model_c = getmodels(C_PTH_PATH, True)
    model_s = getmodels(S_PTH_PATH, True)
    ffn = getmodels(FFN_PTH_PATH, False)

    # input size
    resnet_input_size = (1, 300, 257)
    ffn_input_size = (1, 1, 3*512)

    # jit convertion
    input_b = torch.zeros(resnet_input_size).cuda()
    input_c = torch.zeros(resnet_input_size).cuda()
    input_s = torch.zeros(resnet_input_size).cuda()

    output_b = model_b(input_b)
    output_c = model_c(input_c)
    output_s = model_s(input_s)

    inter_output = torch.cat((output_b,output_c,output_s),1)
            
    output = ffn(inter_output)
    print(output)
    Softmax = torch.nn.Softmax(dim=1)
    output = Softmax(output)
    print(output)
    p = float(output.cpu()[0][1].item())
    print(p)
    # convertion(model_b, B_JIT_PATH)
    # convertion(model_c, C_JIT_PATH)
    # convertion(model_s, S_JIT_PATH)
    # convertion(ffn, FFN_JIT_PATH)

if __name__ == '__main__':
    main()