package de.adorsys.datasafe.business.impl.service;

import de.adorsys.datasafe.business.api.DocusafeServiceDI;
import de.adorsys.datasafe.business.api.deployment.credentials.dto.SystemCredentials;
import de.adorsys.datasafe.business.api.deployment.desired.DSDocument;
import de.adorsys.datasafe.business.api.deployment.desired.DocumentDirectoryFQN;
import de.adorsys.datasafe.business.api.deployment.desired.DocumentFQN;
import de.adorsys.datasafe.business.api.types.DFSAccess;
import de.adorsys.datasafe.business.api.types.DFSCredentials;
import de.adorsys.datasafe.business.api.types.UserID;
import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.file.FileIn;
import de.adorsys.datasafe.business.api.types.file.FileMeta;
import de.adorsys.datasafe.business.api.types.file.FileOut;
import de.adorsys.datasafe.business.api.types.inbox.InboxBucketPath;
import de.adorsys.datasafe.business.api.types.inbox.InboxReadRequest;
import de.adorsys.datasafe.business.api.types.inbox.InboxWriteRequest;
import de.adorsys.datasafe.business.api.types.privatespace.PrivateBucketPath;
import de.adorsys.datasafe.business.api.types.privatespace.PrivateReadRequest;
import de.adorsys.datasafe.business.api.types.privatespace.PrivateWriteRequest;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPrivateProfile;
import de.adorsys.datasafe.business.api.types.profile.CreateUserPublicProfile;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.types.ListRecursiveFlag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.datasafe.business.impl.profile.DFSSystem.CREDS_ID;

public class DocusafeServiceAdapterImpl implements DocusafeServiceDI {

    private DefaultDocusafeServices docusafeService = DaggerDefaultDocusafeServices
            .builder()
            .build();

    /**
     * Essentially means update user profile with following access details.
     */
    @Override
    public void registerDFS(DFSCredentials dfsCredentials, UserIDAuth userIDAuth) {
        throw new UnsupportedOperationException("Register DFS credentials!");
    }

    @Override
    public void storeDocument(DSDocument dsDocument, UserIDAuth userIDAuth) {
        docusafeService.privateService().write(
            new PrivateWriteRequest(
                userIDAuth,
                new FileIn(
                    // TODO: relativize it
                    new FileMeta(dsDocument.getPath().getObjectHandle().getName()),
                    new ByteArrayInputStream(dsDocument.getContent()))
            )
        );
    }

    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        FileOut out = new FileOut(
            new FileMeta(""),
            new ByteArrayOutputStream(1000)
        );

        docusafeService.privateService()
            .read(PrivateReadRequest.builder()
                .owner(userIDAuth)
                .path(new PrivateBucketPath(documentFQN))
                .response(out)
                .build()
            );

        return new DSDocument(documentFQN, out.getData().toString().getBytes());
    }

    @Override
    public void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        throw new UnsupportedOperationException("Private storage access!");
    }

    @Override
    public boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        throw new UnsupportedOperationException("Private storage access!");
    }

    @Override
    public void deleteFolder(DocumentDirectoryFQN documentDirectoryFQN, UserIDAuth userIDAuth) {
        throw new UnsupportedOperationException("Private storage access!");
    }

    @Override
    public List<DocumentFQN> list(DocumentDirectoryFQN documentDirectoryFQN, UserIDAuth userIDAuth, ListRecursiveFlag recursiveFlag) {
        return docusafeService.privateService()
            .list(userIDAuth)
            .map(it -> new DocumentFQN(new BucketPath("private").append(it)))
            .collect(Collectors.toList());
    }

    @Override
    public List<DocumentFQN> listInbox(UserIDAuth userIDAuth) {
        return docusafeService.inboxService()
            .list(userIDAuth)
            .map(DocumentFQN::new)
            .collect(Collectors.toList());
    }

    @Override
    public void writeDocumentToInboxOfUser(DSDocument document, UserID receiverUserID, DocumentFQN destDocumentFQN) {
        docusafeService.inboxService().write(
            new InboxWriteRequest(
                receiverUserID, // FIXME - it must have from
                receiverUserID,
                new FileIn(
                    new FileMeta(document.getPath().getObjectHandle().getName()),
                    new ByteArrayInputStream(document.getContent()))
            )
        );
    }

    @Override
    public DSDocument readDocumentFromInbox(DocumentFQN source, UserIDAuth userIDAuth) {
        FileOut out = new FileOut(
            new FileMeta(""),
            new ByteArrayOutputStream(1000)
        );

        docusafeService.inboxService()
            .read(InboxReadRequest.builder()
                .owner(userIDAuth)
                .path(new InboxBucketPath(source))
                .response(out)
                .build()
            );

        return new DSDocument(source, out.getData().toString().getBytes());
    }

    @Override
    public void deleteDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        throw new UnsupportedOperationException("Private storage access!");
    }

    @Override
    public void createUser(UserIDAuth auth) {

        String userName = auth.getUserID().getValue();

        docusafeService.userProfile().registerPublic(CreateUserPublicProfile.builder()
            .id(auth.getUserID())
            .inbox(access(new BucketPath(userName).append("inbox")))
            .publicKeys(access(new BucketPath(userName).append("keystore")))
            .build()
        );

        docusafeService.userProfile().registerPrivate(CreateUserPrivateProfile.builder()
            .id(auth)
            .privateStorage(access(new BucketPath(userName).append("private")))
            .keystore(access(new BucketPath(userName).append("keystore")))
            .build()
        );
    }

    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
        docusafeService.userProfile().deregister(userIDAuth);
    }

    @Override
    public boolean userExists(UserID userID) {
        return docusafeService.userProfile().userExists(userID);
    }

    private DFSAccess access(BucketPath path) {
        return DFSAccess.builder()
            .physicalPath(path)
            .logicalPath(path)
            .credentials(SystemCredentials.builder().id(CREDS_ID).build())
            .build();
    }
}
