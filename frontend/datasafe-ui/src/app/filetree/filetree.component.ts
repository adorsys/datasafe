import {CollectionViewer, SelectionChange} from '@angular/cdk/collections';
import {FlatTreeControl} from '@angular/cdk/tree';
import {Component, Inject, Injectable} from '@angular/core';
import {BehaviorSubject, merge, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {ApiService} from "../api.service";
import {CredentialsService} from "../credentials.service";
import {Router} from "@angular/router";
import {ErrorMessageUtil} from "../app.component";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material";

class UserFileSystem {

  uiCreatedFolders = new Set<string>();
  fs = new Map<string, Set<string>>();

  buildFs(files: Array<string>) {
    this.fs.clear();
    files.forEach(it => this.addEntry(it));
    this.uiCreatedFolders.forEach(it => this.addEntry(it + "/"));
  }

  rebuildFs() {
    this.uiCreatedFolders.forEach(it => this.addEntry(it + "/"));
  }

  rootLevelNodes() : string[] {
    let res = new Set<string>();

    this.fs.forEach((value, key) => {
      let split = key.split("/", 2);
      res.add(split[0] + (split.length > 1 ? "/" : ""));
    });

    return Array.from(res)
  }

  treeNodeFromPath(path: string): DynamicFlatNode {
    let level = path.split("/").length - 1;
    if (path.endsWith("/")) {
      level = level - 1;
    }

    return new DynamicFlatNode(
        path.replace(/\/$/, "").match(/(.+\/)*([^\/]+)$/)[2],
        path,
        level,
        path.endsWith("/")
    );
  }

  private addEntry(path: string) {

    var fullPath = "";
    var folder = "";
    path.split("/").filter(res => "" !== res).forEach(segment => {
      fullPath += segment;
      fullPath += (fullPath === path ? "" : "/");

      let name = (((fullPath === path) && (!path.endsWith("/"))) ? segment : segment + "/");
      this.putToFolder(folder, name);
      folder = fullPath
    })
  }

  private addDirEntry(path: string) {
    this.putToFolder(path, null);
  }

  private putToFolder(folder: string, name: string) {
    if (folder === "") {
      folder = name;
      name = null;
    }

    if (!this.fs.has(folder)) {
      this.fs.set(folder, new Set<string>());
    }
    this.fs.get(folder).add(name);
  }
}

export class DynamicFlatNode {
  constructor(
      public name: string,
      public path,
      public level = 1,
      public expandable = false,
      public isLoading = false) {}
}


export class DynamicDatabase {
  storageTree = new UserFileSystem();

  loadData(api: ApiService, creds: CredentialsService, filetreeComponent: FiletreeComponent, router: Router) {
    api.listDocuments("", creds.getCredentialsForApi())
        .then(res => {
          this.storageTree.buildFs(<Array<string>> res);

          filetreeComponent.dataSource.data = this.storageTree.rootLevelNodes()
              .map(path => this.storageTree.treeNodeFromPath(path));
        })
        .catch(err => {
          if (err.code == 401 || err.code == 403) {
            router.navigate(['']);
            return;
          }

          filetreeComponent.error = 'Listing storage failed: ' + ErrorMessageUtil.extract(err);
        });
  }

  rebuildView(filetreeComponent: FiletreeComponent) {
    this.storageTree.rebuildFs();
    filetreeComponent.dataSource.data = this.storageTree.rootLevelNodes()
        .map(path => this.storageTree.treeNodeFromPath(path));
  }

  getChildren(node: string): string[] | undefined {
    return Array.from(this.storageTree.fs.get(node)).filter(res => res !== null).map(it => node + it);
  }
}
/**
 * File database, it can build a tree structured Json object from string.
 * Each node in Json object represents a file or a directory. For a file, it has filename and type.
 * For a directory, it has filename and children (a list of files or directories).
 * The input will be a json object string, and the output is a list of `FileNode` with nested
 * structure.
 */
@Injectable()
export class DynamicDataSource {

  dataChange = new BehaviorSubject<DynamicFlatNode[]>([]);

  get data(): DynamicFlatNode[] { return this.dataChange.value; }
  set data(value: DynamicFlatNode[]) {
    this.treeControl.dataNodes = value;
    this.dataChange.next(value);
  }

  constructor(private treeControl: FlatTreeControl<DynamicFlatNode>,
              private database: DynamicDatabase) {}

  connect(collectionViewer: CollectionViewer): Observable<DynamicFlatNode[]> {
    this.treeControl.expansionModel.changed.subscribe(change => {
      if ((change as SelectionChange<DynamicFlatNode>).added ||
          (change as SelectionChange<DynamicFlatNode>).removed) {
        this.handleTreeControl(change as SelectionChange<DynamicFlatNode>);
      }
    });

    return merge(collectionViewer.viewChange, this.dataChange).pipe(map(() => this.data));
  }

  /** Handle expand/collapse behaviors */
  handleTreeControl(change: SelectionChange<DynamicFlatNode>) {
    if (change.added) {
      change.added.forEach(node => this.toggleNode(node, true));
    }
    if (change.removed) {
      change.removed.slice().reverse().forEach(node => this.toggleNode(node, false));
    }
  }

  /**
   * Toggle the node, remove from display list
   */
  toggleNode(node: DynamicFlatNode, expand: boolean) {
    const children = this.database.getChildren(node.path);
    const index = this.data.indexOf(node);
    if (!children || index < 0) { // If no children, or cannot find the node, no op
      return;
    }

    if (expand) {
      const nodes = children.map(path => this.database.storageTree.treeNodeFromPath(path));
      this.data.splice(index + 1, 0, ...nodes);
    } else {
      let count = 0;
      for (let i = index + 1; i < this.data.length && this.data[i].level > node.level; i++, count++) {}
      this.data.splice(index + 1, count);
    }

    this.dataChange.next(this.data);
  }
}

export interface NewFolderData {
  folderPath: string;
}

@Component({
  selector: 'add-folder-dialog',
  templateUrl: 'add.folder.dialog.html',
  styleUrls: ['add.folder.dialog.css'],
})
export class AddFolderDialog {

  constructor(
      public dialogRef: MatDialogRef<AddFolderDialog>,
      @Inject(MAT_DIALOG_DATA) public data: NewFolderData) {}

  onNoClick(): void {
    this.dialogRef.close();
  }
}

/**
 * @title Tree with dynamic data
 */
@Component({
  selector: 'file-tree',
  templateUrl: 'filetree.component.html',
  styleUrls: ['filetree.component.css'],
  providers: [DynamicDatabase]
})
export class FiletreeComponent {

  treeControl: FlatTreeControl<DynamicFlatNode>;
  dataSource: DynamicDataSource;
  getLevel = (node: DynamicFlatNode) => node.level;
  isExpandable = (node: DynamicFlatNode) => node.expandable;
  hasChild = (_: number, _nodeData: DynamicFlatNode) => _nodeData.expandable;
  error: any;

  constructor(private database: DynamicDatabase, private api: ApiService, private creds: CredentialsService,
              private router: Router, public dialog: MatDialog) {
    this.treeControl = new FlatTreeControl<DynamicFlatNode>(this.getLevel, this.isExpandable);
    this.dataSource = new DynamicDataSource(this.treeControl, database);

    database.loadData(api, creds, this, router);
  }

  addUiFolderWithPath(path: string) {
    const dialogRef = this.dialog.open(AddFolderDialog, {
      width: '250px',
      data: {folderPath: ""}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined) {
        this.database.storageTree.uiCreatedFolders.add("" !== path ? path + result : result);
        this.database.rebuildView(this);
      }
    });
  }

  addUiFolder() {
    this.addUiFolderWithPath("");
  }

  addUiFolderWithpathFromName(event) {
    this.addUiFolderWithPath(event.currentTarget.name);
  }

  loadTree() {
    this.error = '';
    this.database.loadData(this.api, this.creds, this, this.router);
  }

  downloadFile(path: string) {
    this.error = '';
    this.api.downloadDocument(path, this.creds.getCredentialsForApi());
  }

  deleteFile(path: string) {
    this.error = '';
    this.api.deleteDocument(path, this.creds.getCredentialsForApi())
        .then(res => this.loadTree())
        .catch(err => this.error = 'Delete failed: ' + ErrorMessageUtil.extract(err));
  }

  uploadFile(event) {
    this.error = '';
    this.api.uploadDocument(event.target.files[0], event.target.files[0].name, this.creds.getCredentialsForApi())
        .then(res => this.loadTree())
        .catch(err => {
          this.error = 'Upload failed: ' + ErrorMessageUtil.extract(err);
        });
  }

  uploadFileWithPathFromName(event) {
    this.error = '';
    this.api.uploadDocument(
        event.currentTarget.files[0],
        event.currentTarget.name + event.currentTarget.files[0].name,
        this.creds.getCredentialsForApi())
        .then(res => this.loadTree())
        .catch(err => {
          this.error = 'Upload failed: ' + ErrorMessageUtil.extract(err);
        });
  }
}
