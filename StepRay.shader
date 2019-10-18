// Each #kernel tells which function to compile; you can have many kernels
#pragma kernel CSMain

// Create a RenderTexture with enableRandomWrite flag and set it
// with cs.SetTexture
RWStructuredBuffer<int> Opacity;
RWStructuredBuffer<int> Visibility;
int Width;
int Height;
int3 Source;


[numthreads(32,1,32)]
void CSMain (uint3 id : SV_DispatchThreadID)
{
	int w = Width;
	int h = Height;
	
	int3 src = Source;
	int3 dst = id;
	
    int3 delta = dst - src;

    int3 progress = int3(0, 0, 0);

    int3 size = abs(delta);
    int3 dir = sign(delta);
    int steps = size.x + size.y + size.z;

    int3 pt = src;
    int visibility = 1;
    for (int step = 0; step < steps; step++) {
        int3 axes = int3(0, 1, 2);
        float3 cmp = float3(
            cmp.x = size.x == 0 ? 9000000 : 1.0 * progress.x / size.x - 0.00001 * size.x,
            cmp.y = size.y == 0 ? 9000000 : 1.0 * progress.y / size.y - 0.00001 * size.y,
            cmp.z = size.z == 0 ? 9000000 : 1.0 * progress.z / size.z - 0.00001 * size.z
        );
        if (cmp.x < cmp.y && cmp.x < cmp.z) {
            progress[0]++;
            pt[0] += dir[0];
        } else if (cmp.y < cmp.x && cmp.y < cmp.z) {
            progress[1]++;
            pt[1] += dir[1];
        } else {
            progress[2]++;
            pt[2] += dir[2];
        }
        
        int index = pt.x + (pt.y + pt.z * h) * w;
        visibility *= (1 - Opacity[index]);
        
    }
    Visibility[dst.x + (dst.y + dst.z * h) * w] = visibility;
}
