import zipfile, shutil, re, os
from pathlib import Path
from lxml import etree

base=Path('/sessions/eager-admiring-dirac/mnt/BanVeTauNhaGa')
src=base/'docs/tài liệu tham khảo/N05_9_Final_Report.docx'
tgt=base/'docs/mydoc/N01_9_REPORT FINAL.docx'
out=base/'docs/mydoc/N01_9_REPORT FINAL_with_cover.docx'
work=Path('/tmp/docx_merge_cover')
if work.exists(): shutil.rmtree(work)
(work/'src').mkdir(parents=True); (work/'tgt').mkdir(parents=True)
with zipfile.ZipFile(src) as z: z.extractall(work/'src')
with zipfile.ZipFile(tgt) as z: z.extractall(work/'tgt')

NS={'w':'http://schemas.openxmlformats.org/wordprocessingml/2006/main','r':'http://schemas.openxmlformats.org/officeDocument/2006/relationships'}
parser=etree.XMLParser(remove_blank_text=False)
sdoc=etree.parse(str(work/'src/word/document.xml'), parser)
tdoc=etree.parse(str(work/'tgt/word/document.xml'), parser)
sbody=sdoc.getroot().find('w:body',NS); tbody=tdoc.getroot().find('w:body',NS)
children=list(sbody)
# take everything before first paragraph whose text looks like table of contents / acknowledgement / intro, fallback first 12 block elems
cut=None
for i,el in enumerate(children):
    if el.tag.endswith('}sectPr'): continue
    txt=''.join(el.xpath('.//w:t/text()', namespaces=NS)).strip().lower()
    if any(k in txt for k in ['table of contents','mục lục','acknowledgement','abstract','introduction','lời cảm ơn']):
        cut=i; break
if cut is None or cut < 1: cut=min(12, max(1, len(children)-1))
cover=[etree.fromstring(etree.tostring(el)) for el in children[:cut] if not el.tag.endswith('}sectPr')]
# add page break after cover
p=etree.Element('{%s}p'%NS['w']); r=etree.SubElement(p,'{%s}r'%NS['w']); br=etree.SubElement(r,'{%s}br'%NS['w']); br.set('{%s}type'%NS['w'],'page'); cover.append(p)

# copy all src media/embeddings/theme assets not already present and relationships with new rIds
src_rels_path=work/'src/word/_rels/document.xml.rels'; tgt_rels_path=work/'tgt/word/_rels/document.xml.rels'
srels=etree.parse(str(src_rels_path), parser); trels=etree.parse(str(tgt_rels_path), parser)
RELNS='http://schemas.openxmlformats.org/package/2006/relationships'
existing={rel.get('Id') for rel in trels.getroot()}
maxid=0
for rid in existing:
    m=re.match(r'rId(\d+)$', rid or '')
    if m: maxid=max(maxid,int(m.group(1)))
relmap={}
for rel in srels.getroot():
    old=rel.get('Id'); target=rel.get('Target')
    if old not in ''.join(etree.tostring(e, encoding='unicode') for e in cover): continue
    maxid+=1; new=f'rId{maxid}'; relmap[old]=new
    n=etree.SubElement(trels.getroot(), '{%s}Relationship'%RELNS)
    n.set('Id',new); n.set('Type',rel.get('Type')); n.set('Target',target)
    if rel.get('TargetMode'): n.set('TargetMode', rel.get('TargetMode'))
    if not rel.get('TargetMode') and target:
        sp=work/'src/word'/target; dp=work/'tgt/word'/target
        if sp.exists():
            dp.parent.mkdir(parents=True, exist_ok=True)
            if not dp.exists(): shutil.copy2(sp, dp)
# replace relationship ids in copied XML
for el in cover:
    xml=etree.tostring(el, encoding='unicode')
    for old,new in relmap.items(): xml=xml.replace(old,new)
    newel=etree.fromstring(xml.encode())
    tbody.insert(0, newel)
# insertion reversed, fix order
inserted=list(tbody)[:len(cover)]
for el in inserted: tbody.remove(el)
for idx,el in enumerate(cover):
    xml=etree.tostring(el, encoding='unicode')
    for old,new in relmap.items(): xml=xml.replace(old,new)
    tbody.insert(idx, etree.fromstring(xml.encode()))

tdoc.write(str(work/'tgt/word/document.xml'), xml_declaration=True, encoding='UTF-8', standalone=True)
trels.write(str(tgt_rels_path), xml_declaration=True, encoding='UTF-8', standalone=True)
if out.exists(): out.unlink()
with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as z:
    for path in (work/'tgt').rglob('*'):
        if path.is_file(): z.write(path, path.relative_to(work/'tgt').as_posix())
print(out)
print('cover_blocks', len(cover), 'relations_copied', len(relmap))
