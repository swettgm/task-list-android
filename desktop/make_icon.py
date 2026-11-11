"""Generate Tasks' rounded gradient checkmark icon using only the standard library."""
import argparse
from pathlib import Path
import struct


def icon_image(size):
    # Rounded checkmark, matching the Android launcher; supersample small icons.
    segments = [(29,55,47,73),(47,73,80,35)]
    def distance(x,y,a,b,c,d):
        t=max(0,min(1,((x-a)*(c-a)+(y-b)*(d-b))/((c-a)**2+(d-b)**2)))
        return ((x-a-t*(c-a))**2+(y-b-t*(d-b))**2)**.5
    pixels=bytearray()
    for y in range(size-1,-1,-1):
        for x in range(size):
            channels=[0,0,0,0]
            for sy in (.25,.75):
                for sx in (.25,.75):
                    px,py=(x+sx)*108/size,(y+sy)*108/size
                    inside=4<=px<=104 and 4<=py<=104 and ((max(0,26-px,px-82))**2+(max(0,26-py,py-82))**2<=22**2)
                    if not inside:continue
                    mark=min(distance(px,py,*line) for line in segments)<=5.5
                    color=[round(a+(b-a)*max(0,min(1,(py-27)/59))) for a,b in [(30,71),(41,83),(53,92)]] if mark else [255]*3
                    for k in range(3):channels[k]+=color[2-k]
                    channels[3]+=255
            # Pixels use straight alpha for Windows icons.
            alpha=round(channels[3]/4)
            pixels.extend([round(v/(channels[3]/255)) if alpha else 0 for v in channels[:3]]+[alpha])
    mask=bytes(((size+31)//32)*4*size)
    header=struct.pack('<IIIHHIIIIII',40,size,size*2,1,32,0,len(pixels)+len(mask),0,0,0,0)
    return header+pixels+mask


def make_icon(destination):
    sizes = [16, 32, 48, 256]
    images = [icon_image(size) for size in sizes]
    offset = 6 + 16 * len(images)
    directory = bytearray(struct.pack('<HHH', 0, 1, len(images)))
    for size, image in zip(sizes, images):
        directory.extend(struct.pack('<BBBBHHII', size if size < 256 else 0, size if size < 256 else 0, 0, 0, 1, 32, len(image), offset))
        offset += len(image)
    Path(destination).write_bytes(bytes(directory) + b''.join(images))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', default=str(Path(__file__).with_name('tasks.ico')))
    make_icon(parser.parse_args().output)
