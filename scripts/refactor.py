import os
import glob
import re
import shutil

def classify(filename):
    name = os.path.basename(filename)
    if name == 'BookstoreApplication.java':
        return ''
    cname = name.lower()
    if 'bookclub' in cname or 'club' in cname: return 'bookclub'
    if 'order' in cname or 'cart' in cname or 'checkout' in cname or 'item' in cname: return 'order'
    if 'book' in cname or 'genre' in cname or 'publisher' in cname or 'stock' in cname or 'sales' in cname: return 'book'
    if 'coupon' in cname or 'discount' in cname: return 'coupon'
    if 'review' in cname or 'rating' in cname: return 'review'
    if 'wishlist' in cname or 'browsing' in cname or 'history' in cname: return 'wishlist'
    if 'chat' in cname or 'message' in cname: return 'chat'
    if 'user' in cname or 'member' in cname or 'auth' in cname or 'login' in cname: return 'user'
    if 'email' in cname: return 'email'
    if 'ecpay' in cname or 'payment' in cname: return 'payment'
    if 'ai' in cname: return 'ai'
    if 'common' in cname or 'base' in cname or 'exception' in cname or 'response' in cname or 'request' in cname: return 'core'
    return 'common'

src_dir = r"c:\Users\Mei\Desktop\Project\BookStore\src\main\java\bookstore"

files = glob.glob(os.path.join(src_dir, '**', '*.java'), recursive=True)

mapping = {} # old_fqn -> new_fqn
cls_to_new_fqn = {} # cls_name -> new_fqn
file_info = []

for f in files:
    rel_path = os.path.relpath(f, src_dir)
    old_pkg = "bookstore"
    dirname = os.path.dirname(rel_path)
    if dirname:
        old_pkg += "." + dirname.replace(os.sep, '.')
    
    cls_name = os.path.splitext(os.path.basename(f))[0]
    old_fqn = f"{old_pkg}.{cls_name}"
    
    domain = classify(f)
    new_pkg = "bookstore" if domain == '' else f"bookstore.{domain}"
    new_fqn = f"{new_pkg}.{cls_name}"
    
    mapping[old_fqn] = new_fqn
    cls_to_new_fqn[cls_name] = new_fqn
    
    file_info.append({
        'path': f,
        'old_pkg': old_pkg,
        'new_pkg': new_pkg,
        'old_fqn': old_fqn,
        'new_fqn': new_fqn,
        'cls_name': cls_name
    })

for info in file_info:
    with open(info['path'], 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 1. Update package
    content = re.sub(r'^package\s+[\w\.]+;', f'package {info["new_pkg"]};', content, flags=re.MULTILINE)
    
    # 2. Remove all existing imports that start with bookstore.
    content = re.sub(r'^import\s+bookstore\.[^;]+;\s*$', '', content, flags=re.MULTILINE)
    
    # 3. Find all used classes that belong to our application
    imports_to_add = set()
    for cls, new_fqn in cls_to_new_fqn.items():
        if cls == info['cls_name']: continue
        # If the class name appears as a word in the file
        if re.search(rf'\b{cls}\b', content):
            new_pkg_for_cls = new_fqn.rsplit('.', 1)[0]
            # If it is in a different package, we must import it
            if new_pkg_for_cls != info['new_pkg']:
                imports_to_add.add(f"import {new_fqn};")
                
    # 4. Remove leftover empty lines near imports to clean up
    content = re.sub(r'\n{3,}', '\n\n', content)
            
    # 5. Insert the new imports right after the package deceleration
    if imports_to_add:
        import_block = "\n".join(sorted(list(imports_to_add)))
        content = re.sub(r'^(package\s+[\w\.]+;)', rf'\1\n\n{import_block}', content, count=1, flags=re.MULTILINE)

    info['new_content'] = content

# Delete all old directories first so we can cleanly write new ones
old_dirs = set(os.path.join(src_dir, d) for d in os.listdir(src_dir) if os.path.isdir(os.path.join(src_dir, d)))
for d in old_dirs:
    shutil.rmtree(d)

# Write all files to new locations
for info in file_info:
    if info['new_pkg'] == 'bookstore':
        new_path = os.path.join(src_dir, info['cls_name'] + '.java')
    else:
        domain = info['new_pkg'].split('.')[-1]
        new_path = os.path.join(src_dir, domain, info['cls_name'] + '.java')
        
    os.makedirs(os.path.dirname(new_path), exist_ok=True)
    with open(new_path, 'w', encoding='utf-8') as f:
        f.write(info['new_content'])

print("Refactoring packages completed.")
