import {CollectionViewer, SelectionChange} from '@angular/cdk/collections';
import {FlatTreeControl} from '@angular/cdk/tree';
import {Component, Injectable} from '@angular/core';
import {BehaviorSubject, merge, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {ApiService} from "../api.service";
import {CredentialsService} from "../credentials.service";
import {Router} from "@angular/router";
import {ErrorMessageUtil} from "../app.component";

class UserFileSystem {

  fs = new Map<string, Set<string>>();

  buildFs(files: Array<string>) {
    this.fs.clear();
    files.forEach(it => this.addEntry(it));
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
    path.split("/").forEach(segment => {
      fullPath += segment;
      fullPath += (fullPath === path ? "" : "/");

      let name = (fullPath === path ? segment : segment + "/");
      this.putToFolder(folder, name);
      folder = fullPath
    })
  }

  private putToFolder(folder: string, name: string) {
    if (folder === "") {
      if (name.endsWith("/")) {
        return;
      }
      folder = name;
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

          filetreeComponent.error = ErrorMessageUtil.extract(err);
        });
  }

  getChildren(node: string): string[] | undefined {
    return Array.from(this.storageTree.fs.get(node)).map(it => node + it);
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
              private router: Router) {
    this.treeControl = new FlatTreeControl<DynamicFlatNode>(this.getLevel, this.isExpandable);
    this.dataSource = new DynamicDataSource(this.treeControl, database);

    database.loadData(api, creds, this, router);
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
        .catch(err => this.error = ErrorMessageUtil.extract(err));
  }

  uploadFile(file) {
    this.error = '';
    console.log('Upload ' + file)
  }
}
